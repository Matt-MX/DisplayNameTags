package com.mattmx.nametags;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.mattmx.nametags.entity.NameTagEntity;
import com.mattmx.nametags.packet.PlayServerEntityMetaDataHandler;
import com.mattmx.nametags.packet.PlayServerSetPassengersHandler;
import com.mattmx.nametags.packet.PlayServerSpawnEntityHandler;
import org.jetbrains.annotations.NotNull;

public class OutgoingPacketListener extends PacketListenerAbstract {
    private final @NotNull NameTags plugin;

    public OutgoingPacketListener(@NotNull NameTags plugin) {
        super(PacketListenerPriority.NORMAL);
        this.plugin = plugin;
    }

    @Override
    public void onPacketSend(@NotNull PacketSendEvent event) {
        switch (event.getPacketType()) {
            case PacketType.Play.Server.SPAWN_ENTITY -> PlayServerSpawnEntityHandler.handlePacket(event);
            case PacketType.Play.Server.ENTITY_METADATA -> PlayServerEntityMetaDataHandler.handlePacket(event);
            case PacketType.Play.Server.SET_PASSENGERS -> PlayServerSetPassengersHandler.handlePacket(event);
            case PacketType.Play.Server.DESTROY_ENTITIES -> {
                WrapperPlayServerDestroyEntities packet = event.getLastUsedWrapper() == null
                    ? new WrapperPlayServerDestroyEntities(event)
                    : (WrapperPlayServerDestroyEntities) event.getLastUsedWrapper();

                for (int entityId : packet.getEntityIds()) {
                    NameTagEntity nameTagEntity = plugin.getEntityManager().getNameTagHolderById(entityId);

                    if (nameTagEntity == null) continue;

                    nameTagEntity.getWrapperEntity().removeViewer(event.getUser());
                }
            }
            default -> {
            }
        }
    }
}
