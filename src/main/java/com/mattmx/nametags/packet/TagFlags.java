package com.mattmx.nametags.packet;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import com.mattmx.nametags.entity.NameTagEntity;
import com.mattmx.nametags.entity.NameTagEntityManager;
import org.jetbrains.annotations.NotNull;

final class TagFlags {
    private static final int SHARED_FLAGS = 0;
    // EntityDataSerializers registers BYTE first in every release, so its id is 0
    private static final int BYTE_SERIALIZER = 0;
    private static final int SHIFT_KEY_DOWN = 0x02;
    private static final int INVISIBLE = 0x20;

    private final NameTagEntityManager tags;

    TagFlags(@NotNull NameTagEntityManager tags) {
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
            // SynchedEntityData packs its entries in index order, so the shared flags lead when present
            int index = ByteBufHelper.readUnsignedByte(buffer);
            if (index != SHARED_FLAGS) {
                return;
            }
            if (ByteBufHelper.readVarInt(buffer) != BYTE_SERIALIZER) {
                return;
            }
            byte flags = ByteBufHelper.readByte(buffer);
            tag.updateFlags((flags & SHIFT_KEY_DOWN) != 0, (flags & INVISIBLE) != 0);
        } finally {
            ByteBufHelper.readerIndex(buffer, start);
        }
    }
}
