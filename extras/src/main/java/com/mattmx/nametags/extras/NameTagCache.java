package com.mattmx.nametags.extras;

import com.mattmx.nametags.extras.schema.CustomNameTag;
import com.mattmx.nametags.extras.storage.NameTagStorageAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class NameTagCache {
    private final @NotNull NameTagsExtras plugin;
    private final Map<String, CustomNameTag> cache = Collections.synchronizedMap(new HashMap<>());

    public NameTagCache(@NotNull NameTagsExtras plugin) {
        this.plugin = plugin;
    }

    public @NotNull CompletableFuture<Optional<CustomNameTag>> loadCacheForPlayer(@NotNull UUID uniqueId) {
        return plugin.getStorage()
            .map((storage) -> CompletableFuture.supplyAsync(() -> storage.getPlayerNameTag(uniqueId)).thenApply((result) -> {
                synchronized (this.cache) {
                    result.ifPresent((nameTag) -> cache.put(uniqueId.toString(), nameTag));
                }
                return result;
            }))
            .orElse(CompletableFuture.failedFuture(NameTagStorageAdapter.storageUnavailable()));
    }

    public @NotNull Optional<CustomNameTag> getCachedNameTag(@NotNull UUID uniqueId) {
        synchronized (this.cache) {
            return Optional.ofNullable(cache.get(uniqueId.toString()));
        }
    }

    public void freeCache(@NotNull String id) {
        synchronized (this.cache) {
            cache.remove(id);
        }
    }

    public void clear() {
        synchronized (this.cache) {
            this.cache.clear();
        }
    }

}
