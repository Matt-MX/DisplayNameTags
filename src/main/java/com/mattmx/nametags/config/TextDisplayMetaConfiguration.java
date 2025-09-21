package com.mattmx.nametags.config;

import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.hook.PapiHook;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class TextDisplayMetaConfiguration {

    public static boolean applyTextMeta(@NotNull ConfigurationSection section, @NotNull TextDisplayMeta to, @NotNull Entity self) {
        Stream<Component> stream = getTextLines(section)
            .stream()
            .map((line) -> convertToComponent(self, line));

        if (NameTags.getInstance().getConfig().getBoolean("defaults.remove-empty-lines", false)) {
            stream = stream.filter(TextComponent.IS_NOT_EMPTY);
        }

        Component text = stream
            .reduce((a, b) -> a.append(Component.newline()).append(b))
            .orElse(null);

        if (text == null) return false;

        if (!text.equals(to.getText())) {
            to.setText(text);
            return true;
        }
        return false;
    }

    private static @NotNull List<String> getTextLines(@NotNull ConfigurationSection section) {
        Object single = section.get("text");

        if (single == null) {
            return Collections.emptyList();
        }

        if (single instanceof List<?>) {
            return section.getStringList("text");
        } else {
            return List.of(single.toString());
        }
    }

    public static void applyBackground(@NotNull ConfigurationSection section, @NotNull TextDisplayMeta to) {
        String backgroundColor = section.getString("background", "transparent");
        int background;

        if (backgroundColor.equalsIgnoreCase("transparent")) {
            background = NameTags.TRANSPARENT;
        } else if (NamedTextColor.NAMES.value(backgroundColor) != null) {
            background = 0x40000000 | Objects.requireNonNull(NamedTextColor.NAMES.value(backgroundColor)).value();
        } else if (backgroundColor.startsWith("#")) {
            String hex = backgroundColor.replace("#", "");

            int rgb;
            int a;
            if (hex.length() == 6) {
                rgb = Integer.parseInt(hex, 16);
                // Set a default alpha of 0x40 (minecraft's internal default)
                a = 0x40;
            } else if (hex.length() == 8) {
                rgb = Integer.parseInt(hex.substring(2), 16);
                a = Integer.parseInt(hex.substring(0, 2), 16);
            } else {
                throw new RuntimeException(String.format("Invalid hex string '#%s'!", hex));
            }

            Color color = Color.fromARGB(rgb).setAlpha(a);

            background = color.asARGB();
        } else {
            background = NameTags.TRANSPARENT;
        }

        if (background != to.getBackgroundColor()) {
            to.setBackgroundColor(background);
        }
    }


    private static Component convertToComponent(Entity self, String line) {
        String formatted = line;

        if (self instanceof Player player) {
            formatted = PapiHook.setPlaceholders(player, formatted);
        }

        return NameTags.getInstance()
            .getFormatter()
            .format(formatted);
    }

}
