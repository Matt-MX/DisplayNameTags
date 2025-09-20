package com.mattmx.nametags.entity;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.entity.trait.TraitHolder;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
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
        traits.destroy();
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

    public void manuallyUpdateVisibility() {
        setVisible(!owner.isInvisible());
    }

    public PacketWrapper<?> getPassengersPacket() {
        return new WrapperPlayServerSetPassengers(getOwner().getEntityId(), getPassengers());
    }

    public int[] getPassengers() {
        NameTagEntityManager manager = NameTags.getInstance().getEntityManager();
        Optional<int[]> cached = manager.getLastSentPassengers(owner.getEntityId());

        if (cached.isPresent()) {
            // Ensure all of our nametag entities are in this list
            int[] existing = cached.get();
            Set<Integer> additional = this.entitiesById.keySet();

            for (int id : existing) {
                additional.remove(id);
            }

            // If the set is empty then all passengers are in the array.
            if (additional.isEmpty()) {
                return existing;
            }

            // Append remaining passengers
            int[] passengers = Arrays.copyOf(existing, existing.length + additional.size());

            int i = 0;
            for (int id : additional) {
                passengers[passengers.length - i - 1] = id;
                i++;
            }

            // Cache so we don't have to do this again.
            manager.setLastSentPassengers(owner.getEntityId(), passengers);
            return passengers;
        }

        return cached.orElseGet(() -> {
            List<Entity> platformPassengers = owner.getPassengers();
            int[] passengers = new int[platformPassengers.size() + entities.size()];

            // Add platform passengers
            for (int i = 0; i < platformPassengers.size(); i++) {
                passengers[i] = platformPassengers.get(i).getEntityId();
            }

            // Add our nametag entities
            for (int i = 0; i < this.entities.size(); i++) {
                passengers[passengers.length - 1 - i] = entities.get(i).getWrapperEntity().getEntityId();
            }

            manager.setLastSentPassengers(owner.getEntityId(), passengers);
            return passengers;
        });
    }

    public void sendPassengerPacket(Player playerViewer) {


    }
}
