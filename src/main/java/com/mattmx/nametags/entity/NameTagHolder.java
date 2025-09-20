package com.mattmx.nametags.entity;

import com.mattmx.nametags.entity.trait.TraitHolder;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class NameTagHolder {
    private @NotNull Entity owner;
    private final @NotNull TraitHolder<NameTagHolder> traits = new TraitHolder<>(this);
    private final @NotNull List<NameTagEntity> entities = new LinkedList<>();
    private final @NotNull Map<Integer, NameTagEntity> entitiesById = new ConcurrentHashMap<>();
    private float cachedViewRange = -1f;

    public NameTagHolder(@NotNull Entity owner) {
        this.owner = owner;
    }

    public @NotNull NameTagEntity createEntity() {
        NameTagEntity entity = new NameTagEntity(this);

        // TODO: add config option for direction?
        // Face downwards to hide debug lines
        entity.getWrapperEntity().rotateHead(0f, 90f);

        entities.add(entity);
        entitiesById.put(entity.getWrapperEntity().getEntityId(), entity);

        return entity;
    }

    public void removeEntity(@NotNull NameTagEntity entity) {
        entities.remove(entity);
        entitiesById.remove(entity.getWrapperEntity().getEntityId());
    }

    public @NotNull List<NameTagEntity> getEntitiesList() {
        return entities;
    }

    public @Nullable NameTagEntity entityByEntityId(int entityId) {
        return entitiesById.get(entityId);
    }

    public void destroy() {
        entitiesById.clear();

        Iterator<NameTagEntity> iterator = entities.iterator();
        while (iterator.hasNext()) {
            final NameTagEntity entry = iterator.next();
            entry.destroy();
            iterator.remove();
        }
    }

    public void setVisible(boolean visible) {
        for (NameTagEntity entity : entities) {
            entity.setVisible(visible);
        }
    }

    public void removeViewer(UUID viewer) {
        for (NameTagEntity entity : entities) {
            entity.getWrapperEntity().removeViewer(viewer);
        }
    }

    public void addViewer(UUID viewer) {
        for (NameTagEntity entity : entities) {
            entity.getWrapperEntity().addViewer(viewer);
        }
    }
}
