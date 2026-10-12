package com.mattmx.nametags.utils.test;

import com.destroystokyo.paper.MaterialTags;
import com.mattmx.nametags.NameTags;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Pig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class TestPassenger implements Listener {

    @EventHandler
    public void onInteract(@NotNull PlayerInteractEvent event) {
        if (!event.hasBlock()) {
            return;
        }

        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        final Block block = event.getClickedBlock();

        if (block == null) {
            return;
        }

        if (!MaterialTags.GLASS.isTagged(block)) {
            return;
        }

        final Location loc = event.getPlayer().getLocation();

        final Pig pig = loc.getWorld().spawn(loc, Pig.class);

        event.getPlayer().addPassenger(pig);

        FoliaScheduler.getEntityScheduler().execute(pig, NameTags.getInstance(), pig::remove, null, 40L);
    }

}
