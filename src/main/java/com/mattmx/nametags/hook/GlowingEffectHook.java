package com.mattmx.nametags.hook;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataType;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.entity.NameTagEntity;
import com.mattmx.nametags.entity.NameTagHolder;
import me.tofaa.entitylib.meta.EntityMeta;
import me.tofaa.entitylib.meta.Metadata;

public class GlowingEffectHook extends PacketListenerAbstract {

    private static final byte MASK_INDEX = 0;
    private static final byte GLOWING_BIT = 64;

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) return;
        final WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(event);

        final NameTagHolder holder = NameTags.getInstance()
            .getEntityManager()
            .getNameTagHolderById(packet.getEntityId());

        if (holder == null) return;

        EntityData<Byte> maskEntry = null;
        for (EntityData<?> entry : packet.getEntityMetadata()) {
            if (entry.getIndex() == MASK_INDEX && entry.getType() == EntityDataTypes.BYTE) {
                maskEntry = (EntityData<Byte>) entry;
            }
        }

        if (maskEntry == null) {
            return;
        }

        boolean isGlowing = (maskEntry.getValue() & GLOWING_BIT) == 1;
        NameTags.getInstance().getExecutor().execute(() -> {
            for (NameTagEntity entity : holder.getEntitiesList()) {
                entity.updateTextMeta((meta) -> meta.setSeeThrough(isGlowing));
            }
        });
    }
}
