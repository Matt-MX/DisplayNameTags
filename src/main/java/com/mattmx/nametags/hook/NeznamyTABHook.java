package com.mattmx.nametags.hook;

import com.mattmx.nametags.NameTags;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.player.PlayerLoadEvent;
import me.neznamy.tab.api.nametag.NameTagManager;
import me.neznamy.tab.api.nametag.UnlimitedNameTagManager;
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

        NameTags plugin = NameTags.getInstance();
        NameTagManager nameTagManager = TabAPI.getInstance().getNameTagManager();

        boolean isUnlimitedNameTag = false;

        try {
            Class.forName("me.neznamy.tab.api.nametag.UnlimitedNameTagManager");
            isUnlimitedNameTag = nameTagManager instanceof UnlimitedNameTagManager;
        } catch (ClassNotFoundException ignored) {
        }

        if (isUnlimitedNameTag) {
            plugin.getLogger().warning("""
                     ⚠ TAB UnlimitedNameTags Mode detected! ⚠
                                    \s
                     DisplayNameTags will attempt to disable this module however
                     we strongly recommend disabling it in TAB's config.
                                    \s
                     This is because both TAB UNT mode and DisplayNameTags attempt
                     to use Passengers to sync positions of custom name tags.
                     Having both could cause some visual issues in-game.
                                    \s
                     Furthermore, the UnlimitedNameTags module is deprecated and
                     will be removed in 5.0.0
                                    \s
                     Read more at https://gist.github.com/NEZNAMY/f4cabf2fd9251a836b5eb877720dee5c
                                    \s
                    \s""");
        } else {
            plugin.getLogger().info("Attempting to override TAB's name tags");
        }

        Objects.requireNonNull(TabAPI.getInstance().getEventBus())
                .register(PlayerLoadEvent.class, (event) -> {
                    final TabPlayer tabPlayer = event.getPlayer();

                    NameTagManager manager = TabAPI.getInstance().getNameTagManager();

                    if (manager != null) {
                        manager.hideNameTag(tabPlayer);
                    }
                });
    }

}
