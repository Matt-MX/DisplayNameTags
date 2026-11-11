package com.mattmx.nametags.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class EntityIdIndex {
    private volatile Int2ObjectOpenHashMap<NameTagEntity> entries = new Int2ObjectOpenHashMap<>();

    @Nullable NameTagEntity get(int entityId) {
        return entries.get(entityId);
    }

    synchronized void put(int entityId, @NotNull NameTagEntity entity) {
        Int2ObjectOpenHashMap<NameTagEntity> next = new Int2ObjectOpenHashMap<>(entries);
        next.put(entityId, entity);
        entries = next;
    }

    synchronized void remove(int entityId, @NotNull NameTagEntity entity) {
        if (entries.get(entityId) != entity) {
            return;
        }
        Int2ObjectOpenHashMap<NameTagEntity> next = new Int2ObjectOpenHashMap<>(entries);
        next.remove(entityId);
        entries = next;
    }

    int size() {
        return entries.size();
    }
}
