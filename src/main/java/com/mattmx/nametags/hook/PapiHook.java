package com.mattmx.nametags.hook;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class PapiHook {

    private PapiHook() {
    }

    public static boolean isPapi() {
        return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    public static @NotNull String setPlaceholders(@NotNull Player player, @NotNull String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }
}
