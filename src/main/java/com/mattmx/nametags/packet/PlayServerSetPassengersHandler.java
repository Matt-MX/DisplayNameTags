package com.mattmx.nametags.packet;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.entity.NameTagEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class PlayServerSetPassengersHandler {

    public static void handlePacket(@NotNull PacketSendEvent event) {
        final NameTags plugin = NameTags.getInstance();
        final WrapperPlayServerSetPassengers packet = new WrapperPlayServerSetPassengers(event);

        final NameTagEntity nameTagEntity = plugin.getEntityManager().getNameTagEntityById(packet.getEntityId());

        if (nameTagEntity == null) return;

        // If the packet doesn't already contain our entity
        boolean containsNameTagPassenger = false;
        for (final int passengerId : packet.getPassengers()) {
            if (passengerId == nameTagEntity.getPassenger().getEntityId()) {
                containsNameTagPassenger = true;
            }
        }

        // TODO(Matt)?: Should we process async and then send another passenger packet afterwards?
        if (!containsNameTagPassenger) {

            // Add our entity
            int[] passengers = Arrays.copyOf(packet.getPassengers(), packet.getPassengers().length + 1);
            passengers[passengers.length - 1] = nameTagEntity.getPassenger().getEntityId();

            packet.setPassengers(passengers);

            NameTags.getInstance()
                .getEntityManager()
                .setLastSentPassengers(packet.getEntityId(), passengers);

            event.markForReEncode(true);
        }
    }

}
