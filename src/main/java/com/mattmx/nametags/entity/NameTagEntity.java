package com.mattmx.nametags.entity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NameTagEntity {
    private final @NotNull Entity bukkitEntity;
    private final @NotNull WrapperEntity passenger;

    public NameTagEntity(@NotNull Entity entity) {
        this.bukkitEntity = entity;
        this.passenger = new WrapperEntity(EntityTypes.TEXT_DISPLAY);

        applyDefaultMeta();
    }

    public void applyDefaultMeta() {
        this.passenger.consumeEntityMeta(TextDisplayMeta.class, (meta) -> {
            meta.setText(bukkitEntity.name());
            meta.setTranslation(new Vector3f(0f, 0.25f, 0f));
            meta.setBackgroundColor(Color.RED.setAlpha(50).asARGB());
            meta.setBillboardConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
        });

        this.passenger.spawn(SpigotConversionUtil.fromBukkitLocation(this.bukkitEntity.getLocation()));

        // TODO: Send packet to player if enabled in config
    }

    public void sendPassengerPacket(Player target) {
        PacketEvents.getAPI()
            .getPlayerManager()
            .sendPacket(target, getPassengersPacket());
    }

    public PacketWrapper<?> getPassengersPacket() {
        return new WrapperPlayServerSetPassengers(bukkitEntity.getEntityId(), new int[]{this.passenger.getEntityId()});
    }

    public Entity getBukkitEntity() {
        return bukkitEntity;
    }

    public WrapperEntity getPassenger() {
        return passenger;
    }
}
