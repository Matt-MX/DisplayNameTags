package com.mattmx.nametags.packet;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.mattmx.nametags.entity.NameTagEntity;
import com.mattmx.nametags.entity.NameTagEntityManager;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

final class TagPassengers {
    private final NameTagEntityManager tags;

    TagPassengers(@NotNull NameTagEntityManager tags) {
        this.tags = tags;
    }

    void sent(@NotNull PacketSendEvent event) {
        NameTagEntity tag = tags.getNameTagEntityById(PacketBytes.peekVarInt(event));
        if (tag == null) {
            return;
        }
        WrapperPlayServerSetPassengers packet = new WrapperPlayServerSetPassengers(event);
        int tagId = tag.getPassenger().getEntityId();
        int[] riders = packet.getPassengers();
        for (int rider : riders) {
            if (rider == tagId) {
                return;
            }
        }
        int[] withTag = Arrays.copyOf(riders, riders.length + 1);
        withTag[riders.length] = tagId;
        packet.setPassengers(withTag);
        tags.setLastSentPassengers(packet.getEntityId(), withTag);
        event.markForReEncode(true);
    }
}
