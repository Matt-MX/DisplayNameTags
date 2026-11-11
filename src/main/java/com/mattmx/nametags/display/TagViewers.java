package com.mattmx.nametags.display;

import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.player.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TagViewers {
    private final Map<UUID, TagViewer> viewers = new ConcurrentHashMap<>();

    @Nullable TagViewer of(@NotNull User user) {
        UUID uuid = user.getUUID();
        if (uuid == null || !ChannelHelper.isOpen(user.getChannel())) {
            return null;
        }
        TagViewer[] replaced = new TagViewer[1];
        TagViewer viewer = viewers.compute(uuid, (key, existing) -> {
            if (existing != null && existing.user() == user) {
                return existing;
            }
            replaced[0] = existing;
            return new TagViewer(user, key);
        });
        if (replaced[0] != null) {
            forget(replaced[0]);
        }
        return viewer;
    }

    public void forget(@NotNull UUID uuid) {
        TagViewer viewer = viewers.remove(uuid);
        if (viewer != null) {
            forget(viewer);
        }
    }

    public int size() {
        return viewers.size();
    }

    private static void forget(@NotNull TagViewer viewer) {
        for (TagDisplay display : viewer.seen()) {
            display.forget(viewer);
        }
        viewer.seen().clear();
    }
}
