package com.mattmx.nametags.hook;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class PapiHook {
    private static final @NotNull Pattern PLACEHOLDER_REGEX = Pattern.compile("%(?!rel_)[^%]+%");
    private static final @NotNull Pattern RELATIVE_PLACEHOLDER_REGEX = Pattern.compile("%[^%]+%");

    public static boolean isPapi() {
        return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    public static String setPlaceholders(Player one, String text) {
        if (!isPapi()) return text;

        String formatted = text;

        formatted = PlaceholderAPI.setPlaceholders(one, formatted);

        return formatted;
    }

    public static Component setPlaceholders(Player one, Component text) {
        if (!isPapi()) return text;

        return text.replaceText(TextReplacementConfig.builder()
                .match(PLACEHOLDER_REGEX)
                .replacement((match, ctx) -> {
                    String matchedText = match.group();
                    String parsed = PlaceholderAPI.setPlaceholders(one, matchedText);
                    return Component.text(parsed);
                })
                .build()
        );
    }

    public static Component setRelationalPlaceholders(Player one, Player two, Component text) {
        if (!isPapi()) return text;

        return text.replaceText(TextReplacementConfig.builder()
                .match(RELATIVE_PLACEHOLDER_REGEX)
                .replacement((match, ctx) -> {
                    String matchedText = match.group();
                    String parsed = PlaceholderAPI.setRelationalPlaceholders(one, two, matchedText);
                    return Component.text(parsed);
                })
                .build()
        );
    }

}
