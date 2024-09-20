package com.mattmx.nametags.hook;

import com.mattmx.nametags.NameTags;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.nametag.NameTagManager;
import me.neznamy.tab.api.nametag.UnlimitedNameTagManager;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class NeznamyTABHook {

    public static void inject(@NotNull NameTags plugin) {
        Bukkit.getScheduler().runTask(plugin, NeznamyTABHook::start);
    }

    private static void start() {
        final boolean isTab = Bukkit.getPluginManager().isPluginEnabled("TAB");

        if (!isTab) return;

        NameTagManager nameTagManager = TabAPI.getInstance().getNameTagManager();

        if (nameTagManager instanceof UnlimitedNameTagManager unlimitedNameTagManager) {
            // TODO(matt): Disable this module somehow?
            // Maybe we need to use the TAB jar as a dependency, i don't think api exposes it.
        }
    }

}
