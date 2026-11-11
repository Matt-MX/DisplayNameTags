package com.mattmx.nametags.packet;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import org.jetbrains.annotations.NotNull;

final class PacketBytes {

    private PacketBytes() {
    }

    static int peekVarInt(@NotNull PacketSendEvent event) {
        Object buffer = event.getByteBuf();
        int start = ByteBufHelper.readerIndex(buffer);
        try {
            return ByteBufHelper.readVarInt(buffer);
        } finally {
            ByteBufHelper.readerIndex(buffer, start);
        }
    }
}
