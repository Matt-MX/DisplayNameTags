package com.mattmx.nametags.entity;

import com.github.retrooper.packetevents.util.Vector3f;
import com.mattmx.nametags.event.NameTagEntityCreateEvent;
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class NameTagEntityManager {
    private final @NotNull ConcurrentHashMap<UUID, NameTagEntity> entityMap = new ConcurrentHashMap<>();
    private @NotNull BiConsumer<Entity, TextDisplayMeta> defaultProvider = (entity, meta) -> {
        // Default minecraft name-tag appearance
        meta.setText(entity.name());
        meta.setTranslation(new Vector3f(0f, 0.2f, 0f));
        meta.setBillboardConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
        meta.setViewRange(50f);
    };

    public @NotNull NameTagEntity getOrCreateNameTagEntity(@NotNull Entity entity) {
        return entityMap.computeIfAbsent(entity.getUniqueId(), (k) -> {
            NameTagEntity newEntity = new NameTagEntity(entity);

            newEntity.getPassenger().consumeEntityMeta(TextDisplayMeta.class, (meta) -> defaultProvider.accept(entity, meta));

            Bukkit.getPluginManager().callEvent(new NameTagEntityCreateEvent(newEntity));

            return newEntity;
        });
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

    public void setDefaultProvider(@NotNull BiConsumer<Entity, TextDisplayMeta> consumer) {
        this.defaultProvider = consumer;
    }
}
