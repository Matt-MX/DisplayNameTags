package com.mattmx.nametags.display;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.protocol.ProtocolManager;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.config.Settings;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TagDisplay {
    private static final Vector3f OLD_CLIENT_OFFSET = new Vector3f(0f, 0.4f, 0f);
    private static final float CENTER_PITCH = 90f;

    private final int entityId;
    private final UUID uuid = UUID.randomUUID();
    private final Entity owner;
    private final TagViewers index;
    private final TagMeta meta = new TagMeta();
    private final RelationalText relational = new RelationalText();
    private final Map<UUID, Sight> sights = new ConcurrentHashMap<>();
    private final Set<UUID> viewerIds = Collections.unmodifiableSet(sights.keySet());
    private final List<EntityData<?>> changes = new ArrayList<>();
    private volatile @Nullable Location location;
    private volatile boolean spawned;
    private float sentPitch;

    private TagDisplay(int entityId, @NotNull Entity owner, @NotNull TagViewers index) {
        this.entityId = entityId;
        this.owner = owner;
        this.index = index;
    }

    public static @NotNull TagDisplay create(@NotNull Entity owner, @NotNull TagViewers index) {
        return new TagDisplay(EntityIds.next(owner.getWorld()), owner, index);
    }

    public static void clearFormatted() {
        RelationalText.clearFormatted();
    }

    public int getEntityId() {
        return entityId;
    }

    public @NotNull UUID getUuid() {
        return uuid;
    }

    public @NotNull TagMeta getMeta() {
        return meta;
    }

    public boolean isSpawned() {
        return spawned;
    }

    public @Nullable Location getLocation() {
        return location;
    }

    public void setLocation(@NotNull Location location) {
        this.location = location;
    }

    public @NotNull Set<UUID> getViewers() {
        return viewerIds;
    }

    public boolean hasViewer(@NotNull UUID uuid) {
        return sights.containsKey(uuid);
    }

    public synchronized void spawn(@NotNull Location location) {
        if (spawned) {
            return;
        }
        this.location = location;
        this.spawned = true;
        changes.clear();
        meta.collectChanges(changes);
        sentPitch = pitch();
        for (Sight sight : sights.values()) {
            sendSpawn(sight);
        }
    }

    public synchronized void despawn() {
        if (!spawned) {
            return;
        }
        spawned = false;
        WrapperPlayServerDestroyEntities destroy = new WrapperPlayServerDestroyEntities(entityId);
        for (Sight sight : sights.values()) {
            sight.viewer.seen().remove(this);
            sight.viewer.user().sendPacket(destroy);
        }
        sights.clear();
    }

    public void addViewer(@NotNull UUID uuid) {
        ProtocolManager protocol = PacketEvents.getAPI().getProtocolManager();
        Object channel = protocol.getChannel(uuid);
        User user = channel == null ? null : protocol.getUser(channel);
        if (user != null) {
            addViewer(user);
        }
    }

    public void addViewer(@NotNull User user) {
        TagViewer viewer = index.of(user);
        if (viewer == null) {
            return;
        }
        synchronized (this) {
            Sight sight = new Sight(viewer);
            sights.put(viewer.uuid(), sight);
            viewer.seen().add(this);
            if (spawned) {
                sendSpawn(sight);
            }
        }
    }

    public void removeViewer(@NotNull User user) {
        UUID uuid = user.getUUID();
        if (uuid != null) {
            removeViewer(uuid);
        }
    }

    public synchronized void removeViewer(@NotNull UUID uuid) {
        Sight sight = sights.remove(uuid);
        if (sight == null) {
            return;
        }
        sight.viewer.seen().remove(this);
        if (spawned) {
            sight.viewer.user().sendPacket(new WrapperPlayServerDestroyEntities(entityId));
        }
    }

    synchronized void forget(@NotNull TagViewer viewer) {
        Sight sight = sights.get(viewer.uuid());
        if (sight != null && sight.viewer == viewer) {
            sights.remove(viewer.uuid());
        }
    }

    public void refresh() {
        send(true);
    }

    public void refreshMeta() {
        send(false);
    }

    private synchronized void send(boolean withText) {
        changes.clear();
        meta.collectChanges(changes);
        if (!spawned) {
            return;
        }
        float pitch = pitch();
        boolean rotate = Float.compare(pitch, sentPitch) != 0;
        sentPitch = pitch;
        Settings settings = NameTags.getInstance().getSettings();
        boolean perViewerText = withText && hasRelationalText(settings);
        List<EntityData<?>> sharedWithText = null;
        for (Sight sight : sights.values()) {
            User user = sight.viewer.user();
            if (rotate) {
                user.sendPacket(new WrapperPlayServerEntityRotation(entityId, 0f, pitch, true));
            }
            Component text = withText ? textFor(sight, settings, false) : null;
            if (text == null && changes.isEmpty()) {
                continue;
            }
            if (!sight.viewer.oldClient()) {
                if (text == null) {
                    user.sendPacket(new WrapperPlayServerEntityMetadata(entityId, changes));
                    continue;
                }
                if (!perViewerText) {
                    if (sharedWithText == null) {
                        sharedWithText = withText(changes, text, false);
                    }
                    user.sendPacket(new WrapperPlayServerEntityMetadata(entityId, sharedWithText));
                    continue;
                }
            }
            user.sendPacket(new WrapperPlayServerEntityMetadata(entityId, withText(changes, text, sight.viewer.oldClient())));
        }
    }

    private boolean hasRelationalText(@NotNull Settings settings) {
        return settings.relationalPlaceholders()
            && owner instanceof Player
            && relational.placeholders(meta.getText(), meta.textVersion()).length > 0;
    }

    private static @NotNull List<EntityData<?>> withText(@NotNull List<EntityData<?>> changes, @Nullable Component text, boolean oldClient) {
        List<EntityData<?>> entries = new ArrayList<>(changes.size() + 1);
        entries.addAll(changes);
        if (oldClient) {
            offsetTranslation(entries);
        }
        if (text != null) {
            entries.add(new EntityData<>(DisplayIndices.onServer(DisplayIndices.TEXT), EntityDataTypes.ADV_COMPONENT, text));
        }
        return entries;
    }

    private void sendSpawn(@NotNull Sight sight) {
        Location at = location;
        if (at == null) {
            return;
        }
        User user = sight.viewer.user();
        user.sendPacket(new WrapperPlayServerSpawnEntity(
            entityId,
            Optional.of(uuid),
            EntityTypes.TEXT_DISPLAY,
            at.getPosition(),
            pitch(),
            at.getYaw(),
            at.getYaw(),
            0,
            Optional.empty()
        ));
        List<EntityData<?>> entries = new ArrayList<>(16);
        meta.collectAll(entries);
        if (sight.viewer.oldClient()) {
            offsetTranslation(entries);
        }
        Component text = textFor(sight, NameTags.getInstance().getSettings(), true);
        entries.add(new EntityData<>(DisplayIndices.onServer(DisplayIndices.TEXT), EntityDataTypes.ADV_COMPONENT, text));
        user.sendPacket(new WrapperPlayServerEntityMetadata(entityId, entries));
    }

    private float pitch() {
        return meta.getBillboardConstraints() == TagMeta.BillboardConstraints.CENTER ? CENTER_PITCH : 0f;
    }

    private @Nullable Component textFor(@NotNull Sight sight, @NotNull Settings settings, boolean force) {
        int version = meta.textVersion();
        Component text = meta.getText();
        String[] placeholders = settings.relationalPlaceholders() && owner instanceof Player
            ? relational.placeholders(text, version)
            : RelationalText.NONE;
        Player viewer = placeholders.length == 0 ? null : sight.viewer.player();
        if (viewer == null) {
            if (!force && sight.textVersion == version) {
                return null;
            }
            sight.textVersion = version;
            sight.values = RelationalText.NONE;
            return text;
        }
        String[] values = RelationalText.values(placeholders, viewer, (Player) owner);
        if (!force && sight.textVersion == version && Arrays.equals(values, sight.values)) {
            return null;
        }
        sight.textVersion = version;
        sight.values = values;
        return RelationalText.apply(text, placeholders, values, settings);
    }

    private static void offsetTranslation(@NotNull List<EntityData<?>> entries) {
        int index = DisplayIndices.onServer(DisplayIndices.TRANSLATION);
        for (int i = 0; i < entries.size(); i++) {
            EntityData<?> entry = entries.get(i);
            if (entry.getIndex() == index && entry.getValue() instanceof Vector3f translation) {
                entries.set(i, new EntityData<>(index, EntityDataTypes.VECTOR3F, translation.add(OLD_CLIENT_OFFSET)));
                return;
            }
        }
    }

    private static final class Sight {
        private final TagViewer viewer;
        private int textVersion = -1;
        private String[] values = RelationalText.NONE;

        private Sight(@NotNull TagViewer viewer) {
            this.viewer = viewer;
        }
    }
}
