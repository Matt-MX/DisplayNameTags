package com.mattmx.nametags.entity;

import com.github.retrooper.packetevents.util.Vector3f;
import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.event.NameTagEntityCreateEvent;
import com.mattmx.nametags.event.NameTagEntityPreSpawnEvent;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class NameTagEntityManager {

    // Entries never expire on their own, sweepStaleEntities() evicts the ones whose entity is gone.
    private final ConcurrentHashMap<UUID, NameTagEntity> nameTagCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, NameTagEntity> nameTagEntityByEntityId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, NameTagEntity> nameTagEntityByPassengerEntityId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, int[]> lastSentPassengers = new ConcurrentHashMap<>();

    private @NotNull BiConsumer<Entity, TextDisplayMeta> defaultProvider = (entity, meta) -> {
        meta.setText(entity.name());
        meta.setTranslation(new Vector3f(0f, 0.2f, 0f));
        meta.setBillboardConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
        meta.setViewRange(50f);
    };

    public @NotNull NameTagEntity getOrCreateNameTagEntity(@NotNull Entity entity) {
        final NameTagEntity existing = nameTagCache.get(entity.getUniqueId());
        if (existing != null && existing.getBukkitEntity().getEntityId() != entity.getEntityId()) {
            if (nameTagCache.remove(entity.getUniqueId(), existing)) {
                discard(existing);
            }
        }

        return nameTagCache.computeIfAbsent(entity.getUniqueId(), uuid -> {
            NameTagEntity newlyCreated = new NameTagEntity(entity);

            newlyCreated.getPassenger().consumeEntityMeta(TextDisplayMeta.class, meta ->
                defaultProvider.accept(entity, meta)
            );

            Bukkit.getPluginManager().callEvent(new NameTagEntityPreSpawnEvent(newlyCreated));

            newlyCreated.initialize();

            Bukkit.getPluginManager().callEvent(new NameTagEntityCreateEvent(newlyCreated));

            final NameTagEntity previous = nameTagEntityByEntityId.put(entity.getEntityId(), newlyCreated);
            nameTagEntityByPassengerEntityId.put(newlyCreated.getPassenger().getEntityId(), newlyCreated);

            if (previous != null && previous != newlyCreated) {
                nameTagEntityByPassengerEntityId.remove(previous.getPassenger().getEntityId(), previous);
                lastSentPassengers.remove(entity.getEntityId());
                previous.destroy();
            }

            return newlyCreated;
        });
    }

    public @Nullable NameTagEntity removeEntity(@NotNull Entity entity) {
        lastSentPassengers.remove(entity.getEntityId());

        final NameTagEntity removed = nameTagEntityByEntityId.remove(entity.getEntityId());
        if (removed != null) {
            nameTagEntityByPassengerEntityId.remove(removed.getPassenger().getEntityId(), removed);
        }

        final NameTagEntity cached = nameTagCache.remove(entity.getUniqueId());
        if (cached != null && cached != removed) {
            if (removed == null) {
                unlink(cached);
                return cached;
            }
            discard(cached);
        }

        return removed;
    }

    public @Nullable NameTagEntity getNameTagEntity(@NotNull Entity entity) {
        return nameTagCache.get(entity.getUniqueId());
    }

    public @Nullable NameTagEntity getNameTagEntityByUUID(UUID uuid) {
        return nameTagCache.get(uuid);
    }

    public @Nullable NameTagEntity getNameTagEntityById(int entityId) {
        return nameTagEntityByEntityId.get(entityId);
    }

    public @Nullable NameTagEntity getNameTagEntityByTagEntityId(int tagEntityId) {
        return nameTagEntityByPassengerEntityId.get(tagEntityId);
    }

    public @NotNull Map<UUID, NameTagEntity> getMappedEntities() {
        return nameTagCache;
    }

    public @NotNull Collection<NameTagEntity> getAllEntities() {
        return nameTagCache.values();
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

    public int getCacheSize() {
        return nameTagCache.size();
    }

    public int getEntityIdMapSize() {
        return nameTagEntityByEntityId.size();
    }

    public int getPassengerIdMapSize() {
        return nameTagEntityByPassengerEntityId.size();
    }

    public int getLastSentPassengersSize() {
        return lastSentPassengers.size();
    }

    public void sweepStaleEntities() {
        final NameTags plugin = NameTags.getInstance();

        for (final Map.Entry<UUID, NameTagEntity> entry : nameTagCache.entrySet()) {
            final UUID uuid = entry.getKey();
            final NameTagEntity tagEntity = entry.getValue();
            final Entity entity = tagEntity.getBukkitEntity();

            if (entity instanceof Player player) {
                if (!player.isConnected()) {
                    evict(uuid, tagEntity);
                }
            } else if (plugin.isEnabled()) {
                FoliaScheduler.getEntityScheduler().execute(
                    entity,
                    plugin,
                    () -> {
                        if (!entity.isValid()) {
                            evict(uuid, tagEntity);
                        }
                    },
                    () -> evict(uuid, tagEntity),
                    1L
                );
            }
        }
    }

    // Only discards if this exact tag is still cached, so a newer tag for the same UUID is left alone.
    private void evict(UUID uuid, NameTagEntity tagEntity) {
        if (nameTagCache.remove(uuid, tagEntity)) {
            discard(tagEntity);
        }
    }

    // Removes by the tag's own IDs, never by UUID, which may now belong to a newer entity's tag.
    private void unlink(NameTagEntity tagEntity) {
        final int entityId = tagEntity.getBukkitEntity().getEntityId();
        nameTagEntityByEntityId.remove(entityId, tagEntity);
        nameTagEntityByPassengerEntityId.remove(tagEntity.getPassenger().getEntityId(), tagEntity);
        lastSentPassengers.remove(entityId);
    }

    private void discard(NameTagEntity tagEntity) {
        unlink(tagEntity);
        tagEntity.destroy();
    }
}
