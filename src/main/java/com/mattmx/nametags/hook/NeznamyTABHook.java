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

    private static final int MAX_ATTEMPTS = 30;

    public static void inject(@NotNull NameTags plugin) {
        // Execute on first tick since we don't know when TAB will be available.
        Bukkit.getScheduler().runTask(plugin, () -> start(plugin, 1));
    }

    private static void start(@NotNull NameTags plugin, int attempt) {
        final boolean isTab = Bukkit.getPluginManager().isPluginEnabled("TAB");
        if (!isTab) return;

        // TAB 6.x enables before its API instance exists (it finishes loading later), so retry once a second.
        try {
            TabAPI.getInstance();
        } catch (IllegalStateException e) {
            if (attempt < MAX_ATTEMPTS) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> start(plugin, attempt + 1), 20L);
            } else {
                plugin.getLogger().warning("TAB API never became available, TAB name tags will not be hidden.");
            }
            return;
        }

        Objects.requireNonNull(TabAPI.getInstance().getEventBus()).register(PlayerLoadEvent.class, (event) -> {
            final TabPlayer tabPlayer = event.getPlayer();
            NameTagManager manager = TabAPI.getInstance().getNameTagManager();

            if (manager != null) {
                manager.hideNameTag(tabPlayer);
            }
        });
    }

}
