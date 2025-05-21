package com.mattmx.nametags;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRemoveEntityEffect;
import com.mattmx.nametags.entity.NameTagHolder;
import com.mattmx.nametags.packet.PlayServerEntityMetaDataHandler;
import com.mattmx.nametags.packet.PlayServerSetPassengersHandler;
import com.mattmx.nametags.packet.PlayServerSpawnEntityHandler;
import org.jetbrains.annotations.NotNull;

public class OutgoingPacketListener extends PacketListenerAbstract {
    private final @NotNull NameTags plugin;

    public OutgoingPacketListener(@NotNull NameTags plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPacketSend(@NotNull PacketSendEvent event) {
        switch (event.getPacketType()) {
            case PacketType.Play.Server.SPAWN_ENTITY -> PlayServerSpawnEntityHandler.handlePacket(event);
            case PacketType.Play.Server.ENTITY_METADATA -> PlayServerEntityMetaDataHandler.handlePacket(event);
            case PacketType.Play.Server.SET_PASSENGERS -> PlayServerSetPassengersHandler.handlePacket(event);
            case PacketType.Play.Server.DESTROY_ENTITIES -> {
                WrapperPlayServerDestroyEntities packet = new WrapperPlayServerDestroyEntities(event);

                for (int entityId : packet.getEntityIds()) {
                    NameTagHolder nameTagHolder = plugin.getEntityManager().getNameTagEntityById(entityId);

                    if (nameTagHolder == null) continue;

                    nameTagHolder.getPassenger().removeViewer(event.getUser());
                }
            }
            case PacketType.Play.Server.ENTITY_EFFECT -> {
                // TODO per-player impl (teams may be able to see invisible players)
                final WrapperPlayServerEntityEffect packet = new WrapperPlayServerEntityEffect(event);

                if (packet.getPotionType() != PotionTypes.INVISIBILITY) return;

                final NameTagHolder nameTagHolder = plugin.getEntityManager().getNameTagEntityById(packet.getEntityId());

                if (nameTagHolder == null) return;

                nameTagHolder.updateVisibility(true);
            }
            case PacketType.Play.Server.REMOVE_ENTITY_EFFECT -> {
                // TODO per-player impl (teams may be able to see invisible players)
                final WrapperPlayServerRemoveEntityEffect packet = new WrapperPlayServerRemoveEntityEffect(event);

                if (packet.getPotionType() != PotionTypes.INVISIBILITY) return;

                final NameTagHolder nameTagHolder = plugin.getEntityManager().getNameTagEntityById(packet.getEntityId());

                if (nameTagHolder == null) return;

                nameTagHolder.updateVisibility(false);
            }
            default -> {
            }
        }
    }
}
