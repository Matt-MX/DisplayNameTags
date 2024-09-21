package com.mattmx.nametags.hook;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.potion.PotionType;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRemoveEntityEffect;
import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.entity.NameTagEntity;
import org.jetbrains.annotations.NotNull;

public class InvisibilityEffectHook extends PacketListenerAbstract {

    private final @NotNull NameTags plugin;

    public InvisibilityEffectHook(@NotNull NameTags plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_EFFECT) {
            final WrapperPlayServerEntityEffect packet = new WrapperPlayServerEntityEffect(event);

            if (packet.getPotionType() != PotionTypes.INVISIBILITY) return;

            final NameTagEntity nameTagEntity = plugin.getEntityManager().getNameTagEntityById(packet.getEntityId());

            if (nameTagEntity == null) return;

            nameTagEntity.updateVisibility(true);
        } else if (event.getPacketType() == PacketType.Play.Server.REMOVE_ENTITY_EFFECT) {
            final WrapperPlayServerRemoveEntityEffect packet = new WrapperPlayServerRemoveEntityEffect(event);

            if (packet.getPotionType() != PotionTypes.INVISIBILITY) return;

            final NameTagEntity nameTagEntity = plugin.getEntityManager().getNameTagEntityById(packet.getEntityId());

            if (nameTagEntity == null) return;

            nameTagEntity.updateVisibility(nameTagEntity.getBukkitEntity().isInvisible());
        }
    }
}
