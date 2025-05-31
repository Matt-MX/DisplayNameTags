package com.mattmx.nametags.entity;

import com.github.retrooper.packetevents.util.Vector3f;
import com.mattmx.nametags.event.NameTagEntityCreateEvent;
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class NameTagEntityManager {
    private final @NotNull Map<UUID, NameTagEntity> nameTagByEntityUUID = Collections.synchronizedMap(new HashMap<>());
    private final @NotNull Map<Integer, NameTagEntity> nameTagEntityByEntityId = Collections.synchronizedMap(new WeakHashMap<>());
    private final @NotNull Map<Integer, NameTagEntity> nameTagEntityByPassengerEntityId = Collections.synchronizedMap(new WeakHashMap<>());

    private final Map<Integer, int[]> lastSentPassengers = new ConcurrentHashMap<>();
    private @NotNull BiConsumer<Entity, TextDisplayMeta> defaultProvider = (entity, meta) -> {
        // Default minecraft name-tag appearance
        meta.setText(entity.name());
        meta.setTranslation(new Vector3f(0f, 0.2f, 0f));
        meta.setBillboardConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
        meta.setViewRange(50f);
    };

    public @NotNull NameTagEntity getOrCreateNameTagEntity(@NotNull Entity entity) {
        NameTagEntity nameTagEntity;
        synchronized (nameTagByEntityUUID) {
            if (nameTagByEntityUUID.containsKey(entity.getUniqueId())) {
                return nameTagByEntityUUID.get(entity.getUniqueId());
            }

            nameTagEntity = new NameTagEntity(entity);
            nameTagByEntityUUID.put(entity.getUniqueId(), nameTagEntity);
        }

        nameTagEntity.getPassenger().consumeEntityMeta(TextDisplayMeta.class, (meta) -> defaultProvider.accept(entity, meta));

        Bukkit.getPluginManager().callEvent(new NameTagEntityCreateEvent(nameTagEntity));

        nameTagEntityByEntityId.put(entity.getEntityId(), nameTagEntity);
        nameTagEntityByPassengerEntityId.put(nameTagEntity.getPassenger().getEntityId(), nameTagEntity);

        return nameTagEntity;
    }

    public @Nullable NameTagEntity removeEntity(@NotNull Entity entity) {
        final NameTagEntity nameTagEntity;
        synchronized (nameTagByEntityUUID) {
            nameTagEntity = nameTagByEntityUUID.remove(entity.getUniqueId());
        }

        nameTagEntityByEntityId.remove(entity.getEntityId());

        if (nameTagEntity != null) {
            nameTagEntityByPassengerEntityId.remove(nameTagEntity.getPassenger().getEntityId());
        }

        return nameTagEntity;
    }

    public @Nullable NameTagEntity getNameTagEntity(@NotNull Entity entity) {
        return nameTagByEntityUUID.get(entity.getUniqueId());
    }

    public @Nullable NameTagEntity getNameTagEntityByUUID(UUID uuid) {
        return nameTagByEntityUUID.get(uuid);
    }

    public @Nullable NameTagEntity getNameTagEntityById(int entityId) {
        return nameTagEntityByEntityId.get(entityId);
    }

    public @Nullable NameTagEntity getNameTagEntityByTagEntityId(int entityId) {
        return nameTagEntityByPassengerEntityId.get(entityId);
    }

    public @NotNull Collection<NameTagEntity> getAllEntities() {
        return this.nameTagByEntityUUID.values();
    }

    public void setDefaultProvider(@NotNull BiConsumer<Entity, TextDisplayMeta> consumer) {
        this.defaultProvider = consumer;
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
