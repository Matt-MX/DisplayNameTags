package com.mattmx.nametags;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.mattmx.nametags.entity.NameTagEntity;
import org.jetbrains.annotations.NotNull;

public class OutgoingPacketListener extends PacketListenerAbstract {

    private final @NotNull NameTags plugin;

    public OutgoingPacketListener(@NotNull NameTags plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPacketSend(@NotNull PacketSendEvent event) {
        switch (event.getPacketType()) {
            case PacketType.Play.Server.SPAWN_ENTITY -> {
                WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity(event);

                if (packet.getUUID().isEmpty()) return;

                NameTagEntity nameTagEntity = plugin.getEntityManager().getNameTagEntityByUUID(packet.getUUID().get());

                if (nameTagEntity == null) return;

                // Add passenger and send to player after
                event.getTasksAfterSend().add(() -> {
                    // To avoid name tag moving when being added
                    nameTagEntity.updateLocation();

                    nameTagEntity.getPassenger().removeViewer(event.getUser());
                    nameTagEntity.getPassenger().addViewer(event.getUser());

                    event.getUser().sendPacket(nameTagEntity.getPassengersPacket());
                });
            }
            case PacketType.Play.Server.DESTROY_ENTITIES -> {
                WrapperPlayServerDestroyEntities packet = new WrapperPlayServerDestroyEntities(event);

                for (int entityId : packet.getEntityIds()) {
                    NameTagEntity nameTagEntity = plugin.getEntityManager().getNameTagEntityById(entityId);

                    if (nameTagEntity == null) continue;

                    nameTagEntity.getPassenger().removeViewer(event.getUser());
                }
            }
            default -> {
            }
        }
    }
}