package com.mattmx.nametags.packet;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.entity.NameTagHolder;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PlayServerSetPassengersHandler {

    public static void handlePacket(@NotNull PacketSendEvent event) {
        final NameTags plugin = NameTags.getInstance();

        final PacketSendEvent eventClone = event.clone();
        final WrapperPlayServerSetPassengers packet0 = new WrapperPlayServerSetPassengers(event);

        final NameTagHolder nameTagHolder = plugin.getEntityManager().getNameTagEntityById(packet0.getEntityId());

        if (nameTagHolder == null) {
            eventClone.cleanUp();
            return;
        }

        event.setCancelled(true);
        final WrapperPlayServerSetPassengers packet = new WrapperPlayServerSetPassengers(eventClone);

        // This could prove a concurrency issue, maybe we should keep track of if there is a newer packet processing?
        plugin.getExecutor().execute(() -> {
            // If the packet doesn't already contain our entity
            final Set<Integer> missing = new HashSet<>();
            for (WrapperEntity passenger : nameTagHolder.getPassengers()) {
                missing.add(passenger.getEntityId());
            }

            for (final int passengerEntityId : packet.getPassengers()) {
                missing.remove(passengerEntityId);
            }

            // If nothing is missing then we can cancel any further execution
            if (missing.isEmpty()) {
                return;
            }

            // Add our entities
            int[] passengers = Arrays.copyOf(packet.getPassengers(), packet.getPassengers().length + missing.size());

            int i = 1;
            for (Integer missingId : missing) {
                passengers[passengers.length - i] = missingId;
                i++;
            }

            packet.setPassengers(passengers);

            NameTags.getInstance()
                    .getEntityManager()
                    .setLastSentPassengers(packet.getEntityId(), passengers);

            eventClone.getUser().sendPacketSilently(packet);
        });
    }

}
