package com.mattmx.nametags.entity;

import com.github.retrooper.packetevents.util.Vector3f;
import com.mattmx.nametags.event.NameTagDestroyEvent;
import com.mattmx.nametags.event.NameTagEntityCreateEvent;
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class NameTagManager {
    private final @NotNull Map<UUID, NameTagHolder> nameTagByEntityUUID = Collections.synchronizedMap(new HashMap<>());
    private final @NotNull Map<Integer, NameTagHolder> nameTagEntityByEntityId = Collections.synchronizedMap(new HashMap<>());
    private final @NotNull Map<Integer, NameTagHolder> nameTagEntityByPassengerEntityId = Collections.synchronizedMap(new HashMap<>());

    private final Map<Integer, int[]> lastSentPassengers = new ConcurrentHashMap<>();
    private @NotNull DefaultNameTagProvider defaultProvider = new DefaultNameTagProvider.Builder()
            .addLine((entity, meta) -> {
                // Default minecraft name-tag appearance
                meta.setText(entity.name());
                meta.setTranslation(new Vector3f(0f, 0.2f, 0f));
                meta.setBillboardConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
                meta.setViewRange(50f);
            }).build();

    public @NotNull NameTagHolder getOrCreateNameTagEntity(@NotNull Entity entity) {
        return nameTagByEntityUUID.computeIfAbsent(entity.getUniqueId(), (k) -> {
            NameTagHolder nameTagHolder = new NameTagHolder(entity);

            defaultProvider.apply(nameTagHolder);

            Bukkit.getPluginManager().callEvent(new NameTagEntityCreateEvent(nameTagHolder));

            nameTagEntityByEntityId.put(entity.getEntityId(), nameTagHolder);
            for (WrapperEntity passenger : nameTagHolder.getPassengers()) {
                nameTagEntityByPassengerEntityId.put(passenger.getEntityId(), nameTagHolder);
            }

            return nameTagHolder;
        });
    }

    public @Nullable NameTagHolder removeEntity(@NotNull Entity entity) {
        final NameTagHolder nameTagHolder = nameTagEntityByPassengerEntityId.remove(entity.getEntityId());

        if (nameTagHolder != null) {
            Event event = new NameTagDestroyEvent(nameTagHolder);
            event.callEvent();

            for (WrapperEntity passenger : nameTagHolder.getPassengers()) {
                nameTagEntityByPassengerEntityId.remove(passenger.getEntityId());
            }
        }

        return nameTagByEntityUUID.remove(entity.getUniqueId());
    }

    public @Nullable NameTagHolder getNameTagEntity(@NotNull Entity entity) {
        return nameTagByEntityUUID.get(entity.getUniqueId());
    }

    public @Nullable NameTagHolder getNameTagEntityByUUID(UUID uuid) {
        return nameTagByEntityUUID.get(uuid);
    }

    public @Nullable NameTagHolder getNameTagEntityById(int entityId) {
        return nameTagEntityByEntityId.get(entityId);
    }

    public @Nullable NameTagHolder getNameTagEntityByTagEntityId(int entityId) {
        return nameTagEntityByPassengerEntityId.get(entityId);
    }

    public @NotNull Collection<NameTagHolder> getAllEntities() {
        return this.nameTagByEntityUUID.values();
    }

    public void setDefaultProvider(@NotNull DefaultNameTagProvider provider) {
        this.defaultProvider = provider;
    }

    @Deprecated
    public void setDefaultProvider(@NotNull BiConsumer<Entity, TextDisplayMeta> consumer) {
        this.defaultProvider = new DefaultNameTagProvider(List.of(consumer));
    }

    public void setLastSentPassengers(int entityId, int[] passengers) {
        this.lastSentPassengers.put(entityId, passengers);
    }

    public void removeLastSentPassengersCache(int entityId) {
        this.lastSentPassengers.remove(entityId);
    }

    public @NotNull Optional<int[]> getLastSentPassengers(int entityId) {
        return Optional.ofNullable(this.lastSentPassengers.get(entityId));
    }
}
