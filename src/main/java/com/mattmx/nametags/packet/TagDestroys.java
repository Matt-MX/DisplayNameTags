package com.mattmx.nametags.packet;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import com.github.retrooper.packetevents.protocol.player.User;
import com.mattmx.nametags.entity.NameTagEntity;
import com.mattmx.nametags.entity.NameTagEntityManager;
import org.jetbrains.annotations.NotNull;

final class TagDestroys {
    private final NameTagEntityManager tags;

    TagDestroys(@NotNull NameTagEntityManager tags) {
        this.tags = tags;
    }

    void sent(@NotNull PacketSendEvent event) {
        Object buffer = event.getByteBuf();
        int start = ByteBufHelper.readerIndex(buffer);
        try {
            int count = ByteBufHelper.readVarInt(buffer);
            User user = null;
            for (int i = 0; i < count; i++) {
                NameTagEntity tag = tags.getNameTagEntityById(ByteBufHelper.readVarInt(buffer));
                if (tag == null) {
                    continue;
                }
                if (user == null) {
                    user = event.getUser();
                }
                tag.getPassenger().removeViewer(user);
            }
        } finally {
            ByteBufHelper.readerIndex(buffer, start);
        }
    }
}
