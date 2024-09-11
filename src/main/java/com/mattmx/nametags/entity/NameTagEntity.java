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
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class NameTagEntity {
    private final @NotNull TraitHolder traits = new TraitHolder(this);
    private final @NotNull Entity bukkitEntity;
    private final @NotNull WrapperEntity passenger;

    public NameTagEntity(@NotNull Entity entity) {
        this.bukkitEntity = entity;
        this.passenger = new WrapperEntity(EntityTypes.TEXT_DISPLAY);

        initialize();
    }

    public void initialize() {
        Location location = updateLocation();

        this.passenger.spawn(location);

        // TODO: Send packet to player if enabled in config
        if (NameTags.getInstance().getConfig().getBoolean("show-self", false)) {

            if (this.bukkitEntity instanceof Player self) {
                this.passenger.addViewer(self.getUniqueId());
                sendPassengerPacket(self);
            }

        }
    }

    public @NotNull TraitHolder getTraits() {
        return traits;
    }

    public void modify(Consumer<TextDisplayMeta> consumer) {
        this.passenger.consumeEntityMeta(TextDisplayMeta.class, consumer);
    }

    public @NotNull TextDisplayMeta getMeta() {
        return this.passenger.getEntityMeta(TextDisplayMeta.class);
    }

    public void sendPassengerPacket(Player target) {
        PacketEvents.getAPI()
            .getPlayerManager()
            .sendPacket(target, getPassengersPacket());
    }

    public PacketWrapper<?> getPassengersPacket() {
        return new WrapperPlayServerSetPassengers(bukkitEntity.getEntityId(), new int[]{this.passenger.getEntityId()});
    }

    public @NotNull Entity getBukkitEntity() {
        return bukkitEntity;
    }

    public @NotNull WrapperEntity getPassenger() {
        return passenger;
    }

    public @NotNull Location updateLocation() {
        Location location = SpigotConversionUtil.fromBukkitLocation(
            bukkitEntity.getLocation()
                .clone()
                .add(0.0, bukkitEntity.getBoundingBox().getMaxY(), 0.0)
        );

        location.setYaw(0f);
        location.setPitch(0f);

        this.passenger.setLocation(location);

        return location;
    }

    public void destroy() {
        this.passenger.despawn();
        this.getTraits().destroy();
    }
}