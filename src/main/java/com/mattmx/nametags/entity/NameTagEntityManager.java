package com.mattmx.nametags.entity;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NameTagEntityManager {
    private final ConcurrentHashMap<UUID, NameTagEntity> entityMap = new ConcurrentHashMap<>();

    public @NotNull NameTagEntity getOrCreateNameTagEntity(@NotNull Entity entity) {
        return entityMap.computeIfAbsent(entity.getUniqueId(), (k) -> new NameTagEntity(entity));
    }

    public @Nullable NameTagEntity removeEntity(@NotNull Entity entity) {
        return entityMap.remove(entity.getUniqueId());
    }

    public @Nullable NameTagEntity getNameTagEntity(@NotNull Entity entity) {
        return entityMap.get(entity.getUniqueId());
    }

    public @Nullable NameTagEntity getNameTagEntityByUUID(UUID uuid) {
        return entityMap.get(uuid);
    }

    public @Nullable NameTagEntity getNameTagEntityById(int entityId) {
        return entityMap.values()
            .stream()
            .filter((e) -> e.getBukkitEntity().getEntityId() == entityId)
            .findFirst()
            .orElse(null);
    }
}
