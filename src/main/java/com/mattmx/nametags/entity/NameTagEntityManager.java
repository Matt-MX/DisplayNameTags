package com.mattmx.nametags.entity;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.mattmx.nametags.NameTagHolderDefaults;
import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.event.NameTagEntityCreateEvent;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NameTagEntityManager {

    private final Cache<UUID, NameTagHolder> nameTagCache = Caffeine.newBuilder()
        .expireAfterAccess(Duration.ofMinutes(1))
        .removalListener(this::handleRemoval)
        .build();

    private final ConcurrentHashMap<Integer, NameTagHolder> nameTagEntityByEntityId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, NameTagHolder> nameTagEntityByPassengerEntityId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, int[]> lastSentPassengers = new ConcurrentHashMap<>();

    @Setter
    private @NotNull NameTagHolderDefaults defaultProvider = NameTagHolderDefaults.vanilla();

    public @NotNull NameTagHolder getOrCreateNameTagHolder(@NotNull Entity entity) {
        NameTagHolder tagEntity = nameTagCache.get(entity.getUniqueId(), uuid -> {
            NameTagHolder holder = new NameTagHolder(entity);

            defaultProvider.applyDefaults(holder);

            Bukkit.getPluginManager().callEvent(new NameTagEntityCreateEvent(holder));

            registerEntities(holder);

            return holder;
        });
        return Objects.requireNonNull(tagEntity, "Cache.get(…) unexpectedly returned null for UUID " + entity.getUniqueId());
    }

    public void registerEntities(@NotNull NameTagHolder holder) {
        for (NameTagEntity entity : holder.getEntitiesList()) {
            nameTagEntityByEntityId.put(entity.getWrapperEntity().getEntityId(), holder);
        }

        nameTagEntityByPassengerEntityId.put(holder.getOwner().getEntityId(), holder);
    }

    public @Nullable NameTagHolder removeEntity(@NotNull Entity entity) {
        final NameTagHolder holder = nameTagCache.getIfPresent(entity.getUniqueId());

        nameTagCache.invalidate(entity.getUniqueId());

        return holder;
    }

    private void removeEntirely(@NotNull Entity entity) {
        lastSentPassengers.remove(entity.getEntityId());
        nameTagCache.invalidate(entity.getUniqueId());

        final NameTagHolder removed = nameTagEntityByEntityId.remove(entity.getEntityId());
        if (removed != null) {
            for (NameTagEntity tag : removed.getEntitiesList()) {
                nameTagEntityByPassengerEntityId.remove(tag.getWrapperEntity().getEntityId());
            }
        }
    }

    public @Nullable NameTagHolder getNameTagHolder(@NotNull Entity entity) {
        return nameTagCache.getIfPresent(entity.getUniqueId());
    }

    public @Nullable NameTagHolder getNameTagHolderByUUID(UUID uuid) {
        return nameTagCache.getIfPresent(uuid);
    }

    public @Nullable NameTagHolder getNameTagHolderById(int entityId) {
        return nameTagEntityByEntityId.get(entityId);
    }

    public @Nullable NameTagHolder getNameTagHolderByTagEntityId(int tagEntityId) {
        return nameTagEntityByPassengerEntityId.get(tagEntityId);
    }

    public @NotNull Map<UUID, NameTagHolder> getMappedEntities() {
        return nameTagCache.asMap();
    }

    public @NotNull Collection<NameTagHolder> getAllHolders() {
        return nameTagCache.asMap().values();
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

    private void handleRemoval(UUID uuid, NameTagHolder holder, RemovalCause cause) {
        if (uuid == null || holder == null) {
            return;
        }

        Entity owner = holder.getOwner();

        if (owner instanceof Player player) {
            if (!player.isOnline() || !player.isConnected()) {
                holder.destroy();
                removeEntirely(owner);
                // Actually remove from map
                this.nameTagCache.cleanUp();
            } else {
                this.nameTagCache.put(uuid, holder);
            }
        } else {
            // Must be run on the main thread, so sync this call
            Bukkit.getScheduler().runTask(NameTags.getInstance(), () -> {
                if (Bukkit.getEntity(uuid) == null) {
                    holder.destroy();
                    removeEntirely(owner);
                    this.nameTagCache.cleanUp();
                } else {
                    this.nameTagCache.put(uuid, holder);
                }
            });
        }
    }
}