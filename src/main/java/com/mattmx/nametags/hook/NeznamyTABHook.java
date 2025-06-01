package com.mattmx.nametags.hook;

import com.mattmx.nametags.NameTags;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.player.PlayerLoadEvent;
import me.neznamy.tab.api.nametag.NameTagManager;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class NeznamyTABHook {

    public static void inject(@NotNull NameTags plugin) {
        // Execute on first tick since we don't know when TAB will be available.
        Bukkit.getScheduler().runTask(plugin, NeznamyTABHook::start);
    }

    private static void start() {
        final boolean isTab = Bukkit.getPluginManager().isPluginEnabled("TAB");
        if (!isTab) return;

        Objects.requireNonNull(TabAPI.getInstance().getEventBus()).register(PlayerLoadEvent.class, (event) -> {
            final TabPlayer tabPlayer = event.getPlayer();
            NameTagManager manager = TabAPI.getInstance().getNameTagManager();

            if (manager != null) {
                manager.hideNameTag(tabPlayer);
            }
        });
    }

}
