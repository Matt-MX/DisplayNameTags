package com.mattmx.nametags.entity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Vector3f;
import com.mattmx.nametags.display.TagMeta;
import com.mattmx.nametags.display.TagViewers;
import com.mattmx.nametags.event.NameTagEntityCreateEvent;
import com.mattmx.nametags.event.NameTagEntityPreSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class NameTagEntityManager {
    private final Map<UUID, NameTagEntity> tags = new ConcurrentHashMap<>();
    private final Map<UUID, NameTagEntity> tagsView = Collections.unmodifiableMap(tags);
    private final EntityIdIndex byEntityId = new EntityIdIndex();
    private final EntityIdIndex byPassengerEntityId = new EntityIdIndex();
    private final Map<Integer, int[]> lastSentPassengers = new ConcurrentHashMap<>();
    private final TagViewers viewers = new TagViewers();

    private volatile @NotNull BiConsumer<Entity, TagMeta> defaultProvider = (entity, meta) -> {
        meta.setText(entity.name());
        meta.setTranslation(new Vector3f(0f, 0.2f, 0f));
        meta.setBillboardConstraints(TagMeta.BillboardConstraints.CENTER);
        meta.setViewRange(50f);
    };

    public @NotNull NameTagEntity getOrCreateNameTagEntity(@NotNull Entity entity) {
        NameTagEntity existing = tags.get(entity.getUniqueId());
        if (existing != null) {
            return existing;
        }
        NameTagEntity created = new NameTagEntity(entity, viewers);
        NameTagEntity raced = tags.putIfAbsent(entity.getUniqueId(), created);
        if (raced != null) {
            return raced;
        }
        created.modify(false, meta -> defaultProvider.accept(entity, meta));
        Bukkit.getPluginManager().callEvent(new NameTagEntityPreSpawnEvent(created));
        byEntityId.put(entity.getEntityId(), created);
        byPassengerEntityId.put(created.getPassenger().getEntityId(), created);
        created.initialize();
        Bukkit.getPluginManager().callEvent(new NameTagEntityCreateEvent(created));
        return created;
    }

    public void showToTrackers(@NotNull NameTagEntity tag) {
        for (Player tracker : tag.getBukkitEntity().getTrackedBy()) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(tracker);
            if (user != null) {
                tag.showTo(user);
            }
        }
    }

    public @Nullable NameTagEntity removeEntity(@NotNull Entity entity) {
        NameTagEntity removed = tags.remove(entity.getUniqueId());
        lastSentPassengers.remove(entity.getEntityId());
        if (removed != null) {
            byEntityId.remove(entity.getEntityId(), removed);
            byPassengerEntityId.remove(removed.getPassenger().getEntityId(), removed);
        }
        return removed;
    }

    public void forgetViewer(@NotNull UUID viewer) {
        viewers.forget(viewer);
    }

    public @Nullable NameTagEntity getNameTagEntity(@NotNull Entity entity) {
        return tags.get(entity.getUniqueId());
    }

    public @Nullable NameTagEntity getNameTagEntityByUUID(@Nullable UUID uuid) {
        return uuid == null ? null : tags.get(uuid);
    }

    public @Nullable NameTagEntity getNameTagEntityById(int entityId) {
        return byEntityId.get(entityId);
    }

    public @Nullable NameTagEntity getNameTagEntityByTagEntityId(int tagEntityId) {
        return byPassengerEntityId.get(tagEntityId);
    }

    public @NotNull Map<UUID, NameTagEntity> getMappedEntities() {
        return tagsView;
    }

    public @NotNull Collection<NameTagEntity> getAllEntities() {
        return tagsView.values();
    }

    public void setDefaultProvider(@NotNull BiConsumer<Entity, TagMeta> consumer) {
        this.defaultProvider = consumer;
    }

    public void setLastSentPassengers(int entityId, int[] passengers) {
        lastSentPassengers.put(entityId, passengers);
    }

    public void removeLastSentPassengersCache(int entityId) {
        lastSentPassengers.remove(entityId);
    }

    public @NotNull Optional<int[]> getLastSentPassengers(int entityId) {
        return Optional.ofNullable(lastSentPassengers.get(entityId));
    }

    public int getCacheSize() {
        return tags.size();
    }

    public int getEntityIdMapSize() {
        return byEntityId.size();
    }

    public int getPassengerIdMapSize() {
        return byPassengerEntityId.size();
    }

    public int getLastSentPassengersSize() {
        return lastSentPassengers.size();
    }

    public int getViewerCount() {
        return viewers.size();
    }
}
