package com.mattmx.nametags.hook;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataType;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.entity.NameTagEntity;
import me.tofaa.entitylib.meta.EntityMeta;
import me.tofaa.entitylib.meta.Metadata;

public class GlowingEffectHook extends PacketListenerAbstract {

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) return;
        final WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(event);

        final NameTagEntity entity = NameTags.getInstance()
            .getEntityManager()
            .getNameTagEntityById(packet.getEntityId());

        if (entity == null) return;

        Metadata meta = new Metadata(packet.getEntityId());

        packet.getEntityMetadata().forEach((entry) -> meta.setIndex((byte) entry.getIndex(), (EntityDataType<Object>) entry.getType(), entry.getValue()));

        EntityMeta wrapper = new EntityMeta(packet.getEntityId(), meta);

        if (wrapper.isGlowing()) {
            entity.modify((tagMeta) -> {
                tagMeta.setSeeThrough(true);
            });
        }
    }
}
