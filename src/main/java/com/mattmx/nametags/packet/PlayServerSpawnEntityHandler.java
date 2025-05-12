package com.mattmx.nametags.packet;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.entity.NameTagEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Responsible for appending the passenger packet with the
 * name tag entity when sending a [WrapperPlayServerSpawnEntity]
 * packet to the client.
 */
public class PlayServerSpawnEntityHandler {

    public static void handlePacket(@NotNull PacketSendEvent event) {
        final NameTags plugin = NameTags.getInstance();
        final WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity(event);

        if (packet.getUUID().isEmpty()) return;

        final NameTagEntity nameTagEntity = plugin.getEntityManager().getNameTagEntityByUUID(packet.getUUID().get());

        if (nameTagEntity == null) return;

        // Add passenger and send to player after (off the netty thread)
        final PacketSendEvent clone = event.clone();
        event.getTasksAfterSend().add(() -> plugin.getExecutor().execute(() -> {
            // To avoid name tag moving when being added
            nameTagEntity.updateLocation();

            // Refreshes as viewer (crusty fix)
            nameTagEntity.getPassenger().removeViewer(clone.getUser());
            nameTagEntity.getPassenger().addViewer(clone.getUser());

            clone.getUser().sendPacket(nameTagEntity.getPassengersPacket());
        }));
    }

}
