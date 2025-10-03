package com.mattmx.nametags.packet;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.entity.NameTagEntity;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

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


        final NameTagEntity nameTagEntity = plugin.getEntityManager().getNameTagEntityByUUID(packet.getUUID().get());

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

    private static void attachPassengerToEntity(NameTagEntity nameTagEntity, User receiver) {
        nameTagEntity.updateLocation();
        nameTagEntity.getPassenger().removeViewer(receiver);
        nameTagEntity.getPassenger().addViewer(receiver);
        receiver.sendPacket(nameTagEntity.getPassengersPacket());
    }

}
