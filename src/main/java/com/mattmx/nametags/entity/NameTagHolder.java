package com.mattmx.nametags.entity;

import com.mattmx.nametags.entity.trait.TraitHolder;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class NameTagHolder {
    private @NotNull Entity owner;
    private final TraitHolder<NameTagHolder> traits = new TraitHolder<>(this);
    private @NotNull Map<Integer, NameTagEntity> entities = new ConcurrentHashMap<>();
    private float cachedViewRange = -1f;

    public NameTagHolder(@NotNull Entity owner) {
        this.owner = owner;
    }

    public @NotNull NameTagEntity createEntity() {
        NameTagEntity entity = new NameTagEntity(this);
        entities.put(entity.getWrapperEntity().getEntityId(), entity);

        return entity;
    }

    public @NotNull Collection<NameTagEntity> getEntitiesList() {
        return entities.values();
    }

    public @Nullable NameTagEntity entityByEntityId(int entityId) {
        return entities.get(entityId);
    }

    public void destroy() {
        Iterator<Map.Entry<Integer, NameTagEntity>> iterator = entities.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, NameTagEntity> entry = iterator.next();

            entry.getValue().destroy();

            iterator.remove();
        }
    }
}
