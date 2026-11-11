package com.mattmx.nametags.display;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class TagViewer {
    private final User user;
    private final UUID uuid;
    private final boolean oldClient;
    private final Set<TagDisplay> seen = ConcurrentHashMap.newKeySet();
    private volatile @Nullable Player player;

    TagViewer(@NotNull User user, @NotNull UUID uuid) {
        this.user = user;
        this.uuid = uuid;
        this.oldClient = user.getClientVersion().isOlderThan(ClientVersion.V_1_20_2);
    }

    @NotNull User user() {
        return user;
    }

    @NotNull UUID uuid() {
        return uuid;
    }

    boolean oldClient() {
        return oldClient;
    }

    @NotNull Set<TagDisplay> seen() {
        return seen;
    }

    @Nullable Player player() {
        Player cached = player;
        if (cached == null || !cached.isOnline()) {
            cached = Bukkit.getPlayer(uuid);
            player = cached;
        }
        return cached;
    }
}
