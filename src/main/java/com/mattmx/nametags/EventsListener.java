package com.mattmx.nametags;

import com.mattmx.nametags.config.TextDisplayMetaConfiguration;
import com.mattmx.nametags.entity.NameTagEntity;
import com.mattmx.nametags.entity.trait.RefreshTrait;
import com.mattmx.nametags.entity.trait.SneakTrait;
import com.mattmx.nametags.event.NameTagEntityCreateEvent;
import org.bukkit.Color;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.jetbrains.annotations.NotNull;

public class EventsListener implements Listener {

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        final NameTagEntity tag = NameTags.getInstance()
            .getEntityManager()
            .getOrCreateNameTagEntity(event.getPlayer());

        tag.updateVisibility();
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        NameTagEntity entity = NameTags.getInstance()
            .getEntityManager()
            .removeEntity(event.getPlayer());

        if (entity == null) return;

        entity.destroy();
    }

    @EventHandler
    public void onPlayerChangeWorld(@NotNull PlayerChangedWorldEvent event) {
        NameTagEntity nameTagEntity = NameTags.getInstance()
            .getEntityManager()
            .getNameTagEntity(event.getPlayer());

        if (nameTagEntity == null) return;

        nameTagEntity.updateLocation();

        if (NameTags.getInstance().getConfig().getBoolean("show-self", false)) {
            nameTagEntity.getPassenger().removeViewer(nameTagEntity.getBukkitEntity().getUniqueId());
            nameTagEntity.getPassenger().addViewer(nameTagEntity.getBukkitEntity().getUniqueId());
            nameTagEntity.sendPassengerPacket(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerSneak(@NotNull PlayerToggleSneakEvent event) {
        if (!NameTags.getInstance().getConfig().getBoolean("sneak.enabled")) {
            return;
        }

        NameTagEntity nameTagEntity = NameTags.getInstance()
            .getEntityManager()
            .getNameTagEntity(event.getPlayer());

        if (nameTagEntity == null) return;

        nameTagEntity.getTraits()
            .getOrAddTrait(SneakTrait.class, SneakTrait::new)
            .updateSneak(event.isSneaking());
    }
}
