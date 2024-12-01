package com.mattmx.nametags;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.mattmx.nametags.entity.NameTagEntity;
import com.mattmx.nametags.entity.trait.SneakTrait;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.jetbrains.annotations.NotNull;

public class EventsListener implements Listener {

    private final @NotNull NameTags plugin;

    public EventsListener(@NotNull NameTags plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        plugin.getEntityManager()
            .getOrCreateNameTagEntity(event.getPlayer())
            .updateVisibility();
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
        for (final NameTagEntity entity : plugin.getEntityManager().getAllEntities()) {
            entity.getPassenger().removeViewer(event.getPlayer().getUniqueId());
        }

        NameTagEntity entity = plugin.getEntityManager()
            .removeEntity(event.getPlayer());

        if (entity != null) {
            entity.destroy();
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(@NotNull PlayerChangedWorldEvent event) {
        NameTagEntity nameTagEntity = plugin.getEntityManager()
            .getNameTagEntity(event.getPlayer());

        if (nameTagEntity == null) return;

        nameTagEntity.updateLocation();

        if (plugin.getConfig().getBoolean("show-self", false)) {
            nameTagEntity.getPassenger().removeViewer(nameTagEntity.getBukkitEntity().getUniqueId());
            nameTagEntity.getPassenger().addViewer(nameTagEntity.getBukkitEntity().getUniqueId());
            nameTagEntity.sendPassengerPacket(event.getPlayer());
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
