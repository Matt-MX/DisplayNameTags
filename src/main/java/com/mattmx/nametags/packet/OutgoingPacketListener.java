package com.mattmx.nametags.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.event.UserDisconnectEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.mattmx.nametags.entity.NameTagEntityManager;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class OutgoingPacketListener extends PacketListenerAbstract {
    private final TagSpawns spawns;
    private final TagPassengers passengers;
    private final TagDestroys destroys;
    private final TagFlags flags;
    private final NameTagEntityManager tags;

    public OutgoingPacketListener(@NotNull NameTagEntityManager tags) {
        super(PacketListenerPriority.NORMAL);
        this.spawns = new TagSpawns(tags);
        this.passengers = new TagPassengers(tags);
        this.destroys = new TagDestroys(tags);
        this.flags = new TagFlags(tags);
        this.tags = tags;
    }

    @Override
    public void onPacketSend(@NotNull PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
            flags.sent(event);
        } else if (event.getPacketType() == PacketType.Play.Server.SPAWN_ENTITY) {
            spawns.sent(event);
        } else if (event.getPacketType() == PacketType.Play.Server.SET_PASSENGERS) {
            passengers.sent(event);
        } else if (event.getPacketType() == PacketType.Play.Server.DESTROY_ENTITIES) {
            destroys.sent(event);
        }
    }

    @Override
    public void onUserDisconnect(@NotNull UserDisconnectEvent event) {
        UUID uuid = event.getUser().getUUID();
        if (uuid != null) {
            tags.forgetViewer(uuid);
        }
    }
}
