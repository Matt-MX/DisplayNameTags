package com.mattmx.nametags.packet;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.entity.NameTagHolder;
import org.jetbrains.annotations.NotNull;

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

        final NameTagHolder nameTagHolder = plugin.getEntityManager().getNameTagEntityByUUID(packet.getUUID().get());

        if (nameTagHolder == null) return;

        // Add passenger and send to player after (off the netty thread)
        final PacketSendEvent clone = event.clone();
        event.getTasksAfterSend().add(() -> plugin.getExecutor().execute(() -> {
            // To avoid name tag moving when being added
            nameTagHolder.updateLocation();

            // Refreshes as viewer (crusty fix)
            nameTagHolder.getPassenger().removeViewer(clone.getUser());
            nameTagHolder.getPassenger().addViewer(clone.getUser());

            clone.getUser().sendPacket(nameTagHolder.getPassengersPacket());
        }));
    }

}
