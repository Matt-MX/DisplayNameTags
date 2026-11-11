package com.mattmx.nametags.display;

import com.mattmx.nametags.config.Settings;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class RelationalText {
    static final String[] NONE = new String[0];

    private static final Pattern PLACEHOLDER = Pattern.compile("%rel_[^%]+%");
    private static final int FORMATTED_LIMIT = 4096;
    private static final Map<String, Component> FORMATTED = new ConcurrentHashMap<>();

    private int version = -1;
    private String[] placeholders = NONE;

    static void clearFormatted() {
        FORMATTED.clear();
    }

    String[] placeholders(@NotNull Component text, int textVersion) {
        if (textVersion != version) {
            Matcher matcher = PLACEHOLDER.matcher(PlainTextComponentSerializer.plainText().serialize(text));
            Set<String> found = new LinkedHashSet<>();
            while (matcher.find()) {
                found.add(matcher.group());
            }
            placeholders = found.isEmpty() ? NONE : found.toArray(NONE);
            version = textVersion;
        }
        return placeholders;
    }

    static String[] values(@NotNull String[] placeholders, @NotNull Player viewer, @NotNull Player owner) {
        String[] values = new String[placeholders.length];
        for (int i = 0; i < placeholders.length; i++) {
            values[i] = PlaceholderAPI.setRelationalPlaceholders(viewer, owner, placeholders[i]);
        }
        return values;
    }

    static @NotNull Component apply(@NotNull Component text, @NotNull String[] placeholders, @NotNull String[] values, @NotNull Settings settings) {
        Component applied = text.replaceText(TextReplacementConfig.builder()
            .match(PLACEHOLDER)
            .replacement((match, builder) -> formatted(settings, valueOf(placeholders, values, match.group())))
            .build());
        if (settings.removeEmptyLines() && applied instanceof TextComponent textComponent) {
            return TextLines.withoutEmptyLines(textComponent);
        }
        return applied;
    }

    private static @NotNull String valueOf(@NotNull String[] placeholders, @NotNull String[] values, @NotNull String placeholder) {
        for (int i = 0; i < placeholders.length; i++) {
            if (placeholders[i].equals(placeholder)) {
                return values[i];
            }
        }
        return placeholder;
    }

    private static @NotNull Component formatted(@NotNull Settings settings, @NotNull String value) {
        Component cached = FORMATTED.get(value);
        if (cached != null) {
            return cached;
        }
        if (FORMATTED.size() >= FORMATTED_LIMIT) {
            FORMATTED.clear();
        }
        Component component = settings.formatter().format(value);
        FORMATTED.put(value, component);
        return component;
    }
}
