package com.mattmx.nametags;

import com.mattmx.nametags.entity.NameTagEntity;
import com.mattmx.nametags.entity.NameTagHolder;
import com.mattmx.nametags.entity.trait.SneakTrait;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffectType;
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

            plugin.getEntityManager().getOrCreateNameTagHolder(event.getPlayer());
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

        // Remove as a viewer from other entities
        for (final NameTagHolder entity : plugin.getEntityManager().getAllHolders()) {
            entity.removeViewer(event.getPlayer().getUniqueId());
        }

        // Remove the leaving players' nametag
        NameTagHolder holder = plugin.getEntityManager().removeEntity(event.getPlayer());

        if (holder != null) {
            holder.destroy();
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(@NotNull PlayerChangedWorldEvent event) {
        // When changing viewers, the nametag's viewers needs to be ideally completely reset, but when does this event fire?
        NameTagHolder holder = plugin.getEntityManager().getNameTagHolder(event.getPlayer());

        if (holder == null) return;

        for (NameTagEntity entity : holder.getEntitiesList()) {
            entity.updateLocation();

            if (plugin.getConfig().getBoolean("show-self", false)) {
                entity.getWrapperEntity().removeViewer(holder.getOwner().getUniqueId());
                entity.getWrapperEntity().addViewer(holder.getOwner().getUniqueId());
                entity.sendPassengerPacket(event.getPlayer());
            }
        }
    }


    @EventHandler
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        NameTagHolder holder = plugin.getEntityManager().getNameTagHolder(event.getPlayer());

        if (holder == null) return;

        if (plugin.getConfig().getBoolean("show-self", false)) {
            // Hides/removes tag on death/respawn screen
            holder.removeViewer(holder.getOwner().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerRespawn(@NotNull PlayerRespawnEvent event) {
        NameTagHolder holder = plugin.getEntityManager().getNameTagHolder(event.getPlayer());

        if (holder == null) return;

        if (plugin.getConfig().getBoolean("show-self", false)) {

            String respawnWorld = event.getRespawnLocation().getWorld().getName();
            String playerWorld = event.getPlayer().getWorld().getName();

            // Ignoring since same action is handled at EventListener#onPlayerChangeWorld if player was killed in another world.
            if (!playerWorld.equalsIgnoreCase(respawnWorld)) return;

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                for (NameTagEntity entity : holder.getEntities()) {
                    // Update entity location.
                    entity.updateLocation();
                    // Add player back as viewer
                    entity.getWrapperEntity().addViewer(holder.getOwner().getUniqueId());
                    // Send passenger packet
                    entity.sendPassengerPacket(event.getPlayer());
                }
            });
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerSneak(@NotNull PlayerToggleSneakEvent event) {
        if (!plugin.getConfig().getBoolean("extra.sneak.enabled")) {
            return;
        }

        if (event.getPlayer().isInsideVehicle()) return;

        NameTagHolder holder = plugin.getEntityManager().getNameTagHolder(event.getPlayer());

        if (holder == null) return;

        for (NameTagEntity entity : holder.getEntities()) {
            entity.getTraits()
                .getOrAddTrait(SneakTrait.class, SneakTrait::new)
                .setSneaking(event.isSneaking());
        }
    }

    @EventHandler
    public void onPlayerEffect(@NotNull EntityPotionEffectEvent event) {
        final NameTagHolder holder = plugin.getEntityManager().getNameTagHolder(event.getEntity());

        if (holder == null) {
            return;
        }

        if (event.getNewEffect() == null && event.getOldEffect() != null && event.getOldEffect().getType() == PotionEffectType.INVISIBILITY) {
            // Has lost invisibility effect (ran out)
            holder.setVisible(true);
        } else if (event.getNewEffect() != null && event.getNewEffect().getType() == PotionEffectType.INVISIBILITY) {
            // Has gained invisibility effect
            holder.setVisible(false);
        }
    }
}
