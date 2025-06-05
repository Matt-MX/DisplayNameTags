package com.mattmx.nametags.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.entity.NameTagEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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

                    attachPassengerToEntity(nameTagEntity0, user);
                }, 1L, TimeUnit.SECONDS);
            }

            return;
        }

        // Add passenger and send to player after (off the netty thread)
        event.getTasksAfterSend().add(() -> plugin.getExecutor().execute(() -> attachPassengerToEntity(nameTagEntity, user)));
    }

    private static void attachPassengerToEntity(final NameTagEntity nameTagEntity, final User receiver) {
        // To avoid name tag moving when being added
        nameTagEntity.updateLocation();

        // Refreshes as viewer (crusty fix)
        nameTagEntity.getPassenger().removeViewer(receiver);
        nameTagEntity.getPassenger().addViewer(receiver);

        receiver.sendPacket(nameTagEntity.getPassengersPacket());
    }

}
