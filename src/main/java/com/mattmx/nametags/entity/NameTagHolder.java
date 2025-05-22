package com.mattmx.nametags.entity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.entity.trait.TraitHolder;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NameTagHolder {
    private final @NotNull TraitHolder traits = new TraitHolder(this);
    private final @NotNull Entity bukkitEntity;
    private final @NotNull List<WrapperEntity> passengers;
    private float cachedViewRange = -1f;
    private final Lock modificationLock = new ReentrantLock();

    public NameTagHolder(@NotNull Entity entity) {
        this.bukkitEntity = entity;
        this.passengers = new LinkedList<>();

        initialize();
    }

    public void initialize() {
        Location location = updateLocation();

        boolean showSelf = NameTags.getInstance()
                .getConfig()
                .getBoolean("show-self", false)
                && this.bukkitEntity instanceof Player;

        for (final WrapperEntity passenger : passengers) {
            passenger.spawn(location);

            if (showSelf) {
                Player self = (Player) this.bukkitEntity;
                passenger.addViewer(self.getUniqueId());
            }
        }

        if (showSelf) {
            sendPassengerPacket((Player) this.bukkitEntity);
        }
    }

    public boolean isInvisible() {
        boolean hasInvisibilityEffect = bukkitEntity instanceof LivingEntity e
                && e.hasPotionEffect(PotionEffectType.INVISIBILITY);

        return bukkitEntity.isInvisible() || hasInvisibilityEffect;
    }

    public void updateVisibility() {
        updateVisibility(isInvisible());
    }

    public void updateVisibility(final boolean isInvisible) {
        modify((meta) -> {
            if (isInvisible && !meta.isInvisible()) {
                this.cachedViewRange = meta.getViewRange();
                meta.setViewRange(0f);
            } else if (!isInvisible && meta.isInvisible()) {
                meta.setViewRange(this.cachedViewRange);
            }
        });
    }

    public @NotNull TraitHolder getTraits() {
        return traits;
    }

    public void modifyAll(Consumer<TextDisplayMeta> consumer) {
        this.passengers.forEach((passenger) -> passenger.consumeEntityMeta(TextDisplayMeta.class, consumer));
    }

    public void modifyAll(BiConsumer<Integer, TextDisplayMeta> consumer) {
        for (int i = 0; i < passengers.size(); i++) {
            consumer.accept(i, getMeta(i));
        }
    }

    @Deprecated
    public void modify(Consumer<TextDisplayMeta> consumer) {
        modifyAll(consumer);
    }

    @Deprecated
    public void modify(int index, Consumer<TextDisplayMeta> consumer) {
        if (index >= this.passengers.size()) {
            return;
        }

        this.passengers.get(index).consumeEntityMeta(TextDisplayMeta.class, consumer);
    }

    @Deprecated
    public @NotNull TextDisplayMeta getMeta() {
        return this.passengers.getFirst().getEntityMeta(TextDisplayMeta.class);
    }

    public @NotNull TextDisplayMeta getMeta(int index) {
        return this.passengers.get(index).getEntityMeta(TextDisplayMeta.class);
    }

    public void sendPassengerPacket(Player target) {
        PacketEvents.getAPI()
                .getPlayerManager()
                .sendPacket(target, getPassengersPacket());
    }

    public PacketWrapper<?> getPassengersPacket() {
        int[] previousPackets = NameTags.getInstance()
                .getEntityManager()
                .getLastSentPassengers(getBukkitEntity().getEntityId())
                .orElseGet(() -> {
                    int[] bukkitPassengers = this.bukkitEntity.getPassengers()
                            .stream()
                            .mapToInt(Entity::getEntityId)
                            .toArray();

                    int[] passengers = Arrays.copyOf(bukkitPassengers, bukkitPassengers.length + this.passengers.size());

                    for (int i = 1; i < this.passengers.size(); i++) {
                        passengers[passengers.length - i] = this.passengers.get(i).getEntityId();
                    }

                    return passengers;
                });

        return new WrapperPlayServerSetPassengers(bukkitEntity.getEntityId(), previousPackets);
    }

    public @NotNull Entity getBukkitEntity() {
        return bukkitEntity;
    }

    public @NotNull List<WrapperEntity> getPassengers() {
        return this.passengers;
    }

    @Deprecated
    public @NotNull WrapperEntity getPassenger() {
        return this.passengers.getFirst();
    }

    public @NotNull Location updateLocation() {
        Location location = SpigotConversionUtil.fromBukkitLocation(
                bukkitEntity.getLocation()
                        .clone()
                        .add(0.0, bukkitEntity.getBoundingBox().getMaxY(), 0.0)
        );

        location.setYaw(0f);
        location.setPitch(0f);

        this.passengers.forEach((passenger) -> passenger.setLocation(location));

        return location;
    }

    public void destroy() {
        this.passengers.forEach(WrapperEntity::despawn);
        this.getTraits().destroy();
    }

    public @NotNull Lock getModificationLock() {
        return this.modificationLock;
    }
}
