package com.mattmx.nametags.hook;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PapiHook {

    public static boolean isPapi() {
        return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    public static String setPlaceholders(Player one, String text) {
        if (!isPapi()) return text;

        String formatted = text;

        formatted = PlaceholderAPI.setPlaceholders(one, formatted);

        return formatted;
    }

    public static String setRelationalPlaceholders(Player one, Player two, String text) {
        if (!isPapi()) return text;

        String formatted = text;

        formatted = PlaceholderAPI.setRelationalPlaceholders(one, two, formatted);

        return formatted;
    }

}
