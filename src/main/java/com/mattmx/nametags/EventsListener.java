package com.mattmx.nametags;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.mattmx.nametags.entity.NameTagEntity;
import com.mattmx.nametags.entity.NameTagEntityManager;
import com.mattmx.nametags.entity.SelfVisibility;
import org.bukkit.Bukkit;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;

public class EventsListener implements Listener {
    private final @NotNull NameTags plugin;

    public EventsListener(@NotNull NameTags plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        NameTagEntityManager tags = plugin.getEntityManager();
        NameTagEntity tag = tags.getOrCreateNameTagEntity(event.getPlayer());
        tag.updateVisibility();
        tags.showToTrackers(tag);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityRemove(@NotNull EntityRemoveFromWorldEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            remove(event.getEntity());
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(@NotNull PlayerChangedWorldEvent event) {
        NameTagEntity tag = plugin.getEntityManager().getNameTagEntity(event.getPlayer());
        if (tag == null) {
            return;
        }
        tag.updateLocation();
        showSelf(tag, event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        NameTagEntity tag = plugin.getEntityManager().getNameTagEntity(event.getPlayer());
        if (tag != null && SelfVisibility.shown(event.getPlayer())) {
            tag.getPassenger().removeViewer(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerRespawn(@NotNull PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        NameTagEntity tag = plugin.getEntityManager().getNameTagEntity(player);
        if (tag == null || !player.getWorld().equals(event.getRespawnLocation().getWorld())) {
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> showSelf(tag, player));
    }

    private void remove(@NotNull Entity entity) {
        NameTagEntity tag = plugin.getEntityManager().removeEntity(entity);
        if (tag != null) {
            tag.destroy();
        }
    }

    private static void showSelf(@NotNull NameTagEntity tag, @NotNull Player player) {
        if (!SelfVisibility.shown(player)) {
            return;
        }
        User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        if (user != null) {
            tag.showTo(user);
        }
    }
}
