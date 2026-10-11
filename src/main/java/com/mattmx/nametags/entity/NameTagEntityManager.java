package com.mattmx.nametags.entity;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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

    // No time-based expiry: tags are removed explicitly on quit. The old expireAfterAccess(1 min)
    // evicted tags of online players nobody had looked up recently; the removal listener then put the
    // OLD tag back, racing with any getOrCreate that had already built a NEW one — leaving the cache
    // and the id maps pointing at different tags, i.e. a tag mounted that no viewer was spawned for.
    private final Cache<UUID, NameTagEntity> nameTagCache = Caffeine.newBuilder().build();

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
        NameTagEntity tagEntity = nameTagCache.get(entity.getUniqueId(), uuid -> {
            NameTagEntity newlyCreated = new NameTagEntity(entity);

            newlyCreated.getPassenger().consumeEntityMeta(TextDisplayMeta.class, meta ->
                defaultProvider.accept(entity, meta)
            );

            Bukkit.getPluginManager().callEvent(new NameTagEntityCreateEvent(newlyCreated));

            nameTagEntityByEntityId.put(entity.getEntityId(), newlyCreated);
            nameTagEntityByPassengerEntityId.put(newlyCreated.getPassenger().getEntityId(), newlyCreated);

            return newlyCreated;
        });
        return Objects.requireNonNull(tagEntity, "Cache.get(…) unexpectedly returned null for UUID " + entity.getUniqueId());
    }

    public @Nullable NameTagEntity removeEntity(@NotNull Entity entity) {
        lastSentPassengers.remove(entity.getEntityId());
        nameTagCache.invalidate(entity.getUniqueId());

        final NameTagEntity removed = nameTagEntityByEntityId.remove(entity.getEntityId());
        if (removed != null) {
            nameTagEntityByPassengerEntityId.remove(removed.getPassenger().getEntityId());
        }

        return removed;
    }

    public @Nullable NameTagEntity getNameTagEntity(@NotNull Entity entity) {
        return nameTagCache.getIfPresent(entity.getUniqueId());
    }

    public @Nullable NameTagEntity getNameTagEntityByUUID(UUID uuid) {
        return nameTagCache.getIfPresent(uuid);
    }

    public @Nullable NameTagEntity getNameTagEntityById(int entityId) {
        return nameTagEntityByEntityId.get(entityId);
    }

    public @Nullable NameTagEntity getNameTagEntityByTagEntityId(int tagEntityId) {
        return nameTagEntityByPassengerEntityId.get(tagEntityId);
    }

    public @NotNull Map<UUID, NameTagEntity> getMappedEntities() {
        return nameTagCache.asMap();
    }

    public @NotNull Collection<NameTagEntity> getAllEntities() {
        return nameTagCache.asMap().values();
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
        return nameTagCache.asMap().size();
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
}
