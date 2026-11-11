package com.mattmx.nametags.packet;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import com.github.retrooper.packetevents.protocol.player.User;
import com.mattmx.nametags.entity.NameTagEntity;
import com.mattmx.nametags.entity.NameTagEntityManager;
import org.jetbrains.annotations.NotNull;

final class TagSpawns {
    private static final int UUID_BYTES = 16;

    private final NameTagEntityManager tags;

    TagSpawns(@NotNull NameTagEntityManager tags) {
        this.tags = tags;
    }

    void sent(@NotNull PacketSendEvent event) {
        Object buffer = event.getByteBuf();
        int start = ByteBufHelper.readerIndex(buffer);
        try {
            NameTagEntity tag = tags.getNameTagEntityById(ByteBufHelper.readVarInt(buffer));
            if (tag == null) {
                return;
            }
            ByteBufHelper.skipBytes(buffer, UUID_BYTES);
            ByteBufHelper.readVarInt(buffer);
            double x = ByteBufHelper.readDouble(buffer);
            double y = ByteBufHelper.readDouble(buffer);
            double z = ByteBufHelper.readDouble(buffer);
            User user = event.getUser();
            event.getTasksAfterSend().add(() -> tag.showTo(user, x, y, z));
        } finally {
            ByteBufHelper.readerIndex(buffer, start);
        }
    }
}
