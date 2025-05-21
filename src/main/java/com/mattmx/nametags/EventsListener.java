package com.mattmx.nametags;

import com.mattmx.nametags.entity.NameTagHolder;
import com.mattmx.nametags.entity.trait.SneakTrait;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.jetbrains.annotations.NotNull;

public class EventsListener implements Listener {

    private final @NotNull NameTags plugin;

    public EventsListener(@NotNull NameTags plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Bukkit.getAsyncScheduler().runNow(plugin, (task) -> {
            if (!event.getPlayer().isOnline()) {
                return;
            }

            plugin.getEntityManager()
                    .getOrCreateNameTagEntity(event.getPlayer())
                    .updateVisibility();
        });

    }

//    @EventHandler
//    public void onEntityRemove(@NotNull EntityRemoveFromWorldEvent event) {
//        plugin.getEntityManager().removeLastSentPassengersCache(event.getEntity().getEntityId());
//
//        NameTagEntity entity = plugin.getEntityManager()
//            .removeEntity(event.getEntity());
//
//        if (entity != null) {
//            entity.destroy();
//        }
//    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        plugin.getEntityManager().removeLastSentPassengersCache(event.getPlayer().getEntityId());

        // Remove as a viewer from all entities
        for (final NameTagHolder entity : plugin.getEntityManager().getAllEntities()) {
            entity.getPassenger().removeViewer(event.getPlayer().getUniqueId());
        }

        NameTagHolder entity = plugin.getEntityManager().removeEntity(event.getPlayer());

        if (entity != null) {
            entity.destroy();
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(@NotNull PlayerChangedWorldEvent event) {
        NameTagHolder nameTagHolder = plugin.getEntityManager()
                .getNameTagEntity(event.getPlayer());

        if (nameTagHolder == null) return;

        nameTagHolder.updateLocation();

        if (plugin.getConfig().getBoolean("show-self", false)) {
            nameTagHolder.getPassenger().removeViewer(nameTagHolder.getBukkitEntity().getUniqueId());
            nameTagHolder.getPassenger().addViewer(nameTagHolder.getBukkitEntity().getUniqueId());
            nameTagHolder.sendPassengerPacket(event.getPlayer());
        }
    }


    @EventHandler
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        NameTagHolder nameTagHolder = plugin.getEntityManager()
                .getNameTagEntity(event.getPlayer());

        if (nameTagHolder == null) return;

        if (plugin.getConfig().getBoolean("show-self", false)) {
            // Hides/removes tag on death/respawn screen
            nameTagHolder.getPassenger().removeViewer(nameTagHolder.getBukkitEntity().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerRespawn(@NotNull PlayerRespawnEvent event) {
        NameTagHolder nameTagHolder = plugin.getEntityManager()
                .getNameTagEntity(event.getPlayer());

        if (nameTagHolder == null) return;

        if (plugin.getConfig().getBoolean("show-self", false)) {

            String respawnWorld = event.getRespawnLocation().getWorld().getName();
            String playerWorld = event.getPlayer().getWorld().getName();
            // Ignoring since same action is handled at EventListener#onPlayerChangeWorld if player was killed in another world.
            if (!playerWorld.equalsIgnoreCase(respawnWorld)) return;

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                // Update entity location.
                nameTagHolder.updateLocation();
                // Add player back as viewer
                nameTagHolder.getPassenger().addViewer(nameTagHolder.getBukkitEntity().getUniqueId());
                // Send passenger packet
                nameTagHolder.sendPassengerPacket(event.getPlayer());
            });
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerSneak(@NotNull PlayerToggleSneakEvent event) {
        if (!plugin.getConfig().getBoolean("sneak.enabled")) {
            return;
        }

        if (event.getPlayer().isInsideVehicle()) return;

        NameTagHolder nameTagHolder = plugin.getEntityManager()
                .getNameTagEntity(event.getPlayer());

        if (nameTagHolder == null) return;

        nameTagHolder.getTraits()
                .getOrAddTrait(SneakTrait.class, SneakTrait::new)
                .updateSneak(event.isSneaking());
    }
}
