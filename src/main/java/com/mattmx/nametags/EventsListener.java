package com.mattmx.nametags;

import com.mattmx.nametags.entity.NameTagEntity;
import com.mattmx.nametags.entity.trait.SneakTrait;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.NotNull;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class EventsListener implements Listener {

    private final @NotNull NameTags plugin;

    public EventsListener(@NotNull NameTags plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(@NotNull PlayerSpawnLocationEvent event) {
        Bukkit.getAsyncScheduler().runNow(plugin, (task) -> {
            if (!event.getPlayer().isConnected()) {
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        plugin.getEntityManager().removeLastSentPassengersCache(event.getPlayer().getEntityId());

        // Remove as a viewer from all entities
        for (final NameTagEntity entity : plugin.getEntityManager().getAllEntities()) {
            entity.getPassenger().removeViewer(event.getPlayer().getUniqueId());
        }

        NameTagEntity entity = plugin.getEntityManager().removeEntity(event.getPlayer());

        if (entity != null) {
            entity.destroy();
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(@NotNull PlayerChangedWorldEvent event) {
        NameTagEntity nameTagEntity = plugin.getEntityManager().getNameTagEntity(event.getPlayer());

        if (nameTagEntity == null) return;

        nameTagEntity.updateLocation();

        if (plugin.getConfig().getBoolean("show-self", false)) {
            nameTagEntity.getPassenger().removeViewer(nameTagEntity.getBukkitEntity().getUniqueId());
            nameTagEntity.getPassenger().addViewer(nameTagEntity.getBukkitEntity().getUniqueId());
            nameTagEntity.sendPassengerPacket(event.getPlayer());
        }
    }


    @EventHandler
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        NameTagEntity nameTagEntity = plugin.getEntityManager()
            .getNameTagEntity(event.getPlayer());

        if (nameTagEntity == null) return;

        if (plugin.getConfig().getBoolean("show-self", false)) {
            // Hides/removes tag on death/respawn screen
            nameTagEntity.getPassenger().removeViewer(nameTagEntity.getBukkitEntity().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerRespawn(@NotNull PlayerRespawnEvent event) {
        NameTagEntity nameTagEntity = plugin.getEntityManager()
            .getNameTagEntity(event.getPlayer());

        if (nameTagEntity == null) return;

        if (plugin.getConfig().getBoolean("show-self", false)) {

            String respawnWorld = event.getRespawnLocation().getWorld().getName();
            String playerWorld = event.getPlayer().getWorld().getName();
            // Ignoring since same action is handled at EventListener#onPlayerChangeWorld if player was killed in another world.
            if (!playerWorld.equalsIgnoreCase(respawnWorld)) return;

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                // Update entity location.
                nameTagEntity.updateLocation();
                // Add player back as viewer
                nameTagEntity.getPassenger().addViewer(nameTagEntity.getBukkitEntity().getUniqueId());
                // Send passenger packet
                nameTagEntity.sendPassengerPacket(event.getPlayer());
            });
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerSneak(@NotNull PlayerToggleSneakEvent event) {
        if (!plugin.getConfig().getBoolean("sneak.enabled")) {
            return;
        }

        if (event.getPlayer().isInsideVehicle()) return;

        NameTagEntity nameTagEntity = plugin.getEntityManager()
            .getNameTagEntity(event.getPlayer());

        if (nameTagEntity == null) return;

        nameTagEntity.getTraits()
            .getOrAddTrait(SneakTrait.class, SneakTrait::new)
            .updateSneak(event.isSneaking());
    }
}
