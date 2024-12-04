package com.mattmx.nametags.extras;

import com.mattmx.nametags.event.NameTagEntityCreateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class NameTagsExtrasListener implements Listener {
    private final @NotNull NameTagsExtras plugin;

    public NameTagsExtrasListener(@NotNull NameTagsExtras plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerPreLogin(@NotNull AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

        try {
            this.plugin.getCache().loadCacheForPlayer((event.getUniqueId())).get();
        } catch (InterruptedException | ExecutionException ex) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to load data for {}: {}", new Object[]{event.getName(), ex.getMessage()});
        }
    }

    @EventHandler
    public void onNameTagCreate(@NotNull NameTagEntityCreateEvent event) {
        this.plugin
            .getCache()
            .getCachedNameTag(event.getNameTag().getBukkitEntity().getUniqueId())
            .ifPresent((customnameTag) -> {
                // TODO apply custom stuff
            });
    }

    @EventHandler
    public void onPlayerDisconnect(@NotNull PlayerQuitEvent event) {
        this.plugin.getCache().freeCache(event.getPlayer().getUniqueId().toString());
    }

}
