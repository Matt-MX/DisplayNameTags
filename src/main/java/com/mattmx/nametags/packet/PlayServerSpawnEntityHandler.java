package com.mattmx.nametags.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.entity.NameTagEntity;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Responsible for appending the name tag spawn packet and
 * passenger packet with the name tag entity when sending
 * a [WrapperPlayServerSpawnEntity]
 * packet to the client.
 */
public class PlayServerSpawnEntityHandler {

    public static void handlePacket(@NotNull PacketSendEvent event) {
        final NameTags plugin = NameTags.getInstance();
        final WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity(event);

        if (packet.getUUID().isEmpty()) return;

        final UUID packetUUID = packet.getUUID().get();
        final NameTagEntity nameTagEntity = plugin.getEntityManager().getNameTagEntityByUUID(packetUUID);

        final User user = event.getUser();
        if (nameTagEntity == null) {

            // If it's a player, and they don't have a name tag yet, retry after a delay.
            if (packet.getEntityType() == EntityTypes.PLAYER) {
                Bukkit.getAsyncScheduler().runDelayed(plugin, (task) -> {
                    final NameTagEntity nameTagEntity0 = plugin.getEntityManager().getNameTagEntityByUUID(packetUUID);

                    if (nameTagEntity0 == null) {
                        return;
                    }

        UUID packetUUID = packet.getUUID().orElse(null);
        User user = event.getUser();
        if (nameTagEntity == null && packetUUID != null) {
            if (packet.getEntityType() == EntityTypes.PLAYER) {
                Bukkit.getAsyncScheduler().runDelayed(plugin, task -> {
                    NameTagEntity nameTagEntity0 = plugin.getEntityManager().getNameTagEntityByUUID(packetUUID);

                    if (nameTagEntity0 == null) {
                        return;
                    }

                    PlayServerSpawnEntityHandler.attachPassengerToEntity(nameTagEntity0, user);
                }, 1L, TimeUnit.SECONDS);
            }
            return;
        }

        event.getTasksAfterSend().add(() -> plugin.getExecutor().execute(() -> PlayServerSpawnEntityHandler.attachPassengerToEntity(nameTagEntity, user)));
    }

    private static void attachPassengerToEntity(@Nullable NameTagEntity nameTagEntity, User receiver) {
        if (nameTagEntity == null) {
            return;
        }

        nameTagEntity.updateLocation();

        nameTagEntity.getPassenger().removeViewer(receiver);
        nameTagEntity.getPassenger().addViewer(receiver);

        receiver.sendPacket(nameTagEntity.getPassengersPacket());
    }

}
