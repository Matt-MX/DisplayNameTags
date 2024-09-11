package com.mattmx.nametags;

import com.github.retrooper.packetevents.protocol.world.Location;
import com.mattmx.nametags.entity.NameTagEntity;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class EventsListener implements Listener {

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        NameTagEntity nameTagEntity = NameTags.getInstance()
            .getEntityManager()
            .getOrCreateNameTagEntity(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        NameTagEntity entity = NameTags.getInstance()
            .getEntityManager()
            .removeEntity(event.getPlayer());

        if (entity == null) return;

        entity.getPassenger().despawn();
    }

    @EventHandler
    public void onChangeWorld(@NotNull PlayerChangedWorldEvent event) {
        NameTagEntity nameTagEntity = NameTags.getInstance()
            .getEntityManager()
            .getNameTagEntity(event.getPlayer());

        if (nameTagEntity == null) return;

        Location newLocation = SpigotConversionUtil.fromBukkitLocation(event.getPlayer().getLocation());

        newLocation.setPitch(0f);
        newLocation.setYaw(0f);

        nameTagEntity.getPassenger().setLocation(newLocation);
    }
}
