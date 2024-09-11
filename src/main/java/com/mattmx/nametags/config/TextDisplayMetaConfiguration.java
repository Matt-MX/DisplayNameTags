package com.mattmx.nametags.config;

import com.mattmx.nametags.NameTags;
import me.clip.placeholderapi.PlaceholderAPI;
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Locale;
import java.util.Objects;

public class TextDisplayMetaConfiguration {

    public static void applyTextMeta(@NotNull ConfigurationSection section, @NotNull TextDisplayMeta to, @NotNull Player self, @NotNull Player sender) {
        Component text = section.getStringList("text")
            .stream()
            .map((line) -> convertToComponent(self, sender, line))
            .reduce((a, b) -> a.append(Component.newline()).append(b))
            .orElse(Component.empty());

        if (!text.equals(to.getText())) {
            to.setText(text);
        }
    }

    public static void applyMeta(@NotNull ConfigurationSection section, @NotNull TextDisplayMeta to) {
        String backgroundColor = section.getString("background", "0x40000000");
        int background;

        if (backgroundColor.equalsIgnoreCase("transparent")) {
            background = NameTags.TRANSPARENT;
        } else if (NamedTextColor.NAMES.value(backgroundColor) != null) {
            background = 0x40000000 | Objects.requireNonNull(NamedTextColor.NAMES.value(backgroundColor)).value();
        } else if (backgroundColor.startsWith("#")) {
            background = new Color((int) Long.parseLong(backgroundColor.replace("#", ""), 16), true).getRGB();
        } else {
            background = NameTags.TRANSPARENT;
        }

        if (background != to.getBackgroundColor()) {
            to.setBackgroundColor(background);
        }

        String billboardString = section.getString("billboard", "center");
        AbstractDisplayMeta.BillboardConstraints billboard = AbstractDisplayMeta.BillboardConstraints.valueOf(billboardString.toUpperCase(Locale.ROOT));
        if (billboard != to.getBillboardConstraints()) {
            to.setBillboardConstraints(billboard);
        }

        boolean shadow = section.getBoolean("shadow");
        if (shadow != to.isShadow()) {
            to.setShadow(shadow);
        }

        String range = section.getString("range", "default");
        float finalRange = range.equalsIgnoreCase("default")
            ? (Bukkit.getSimulationDistance() * 16f)
            : Float.parseFloat(range);

        if (finalRange != to.getViewRange()) {
            to.setViewRange(finalRange);
        }

        String gap = section.getString("gap", "default");
        float finalGap = gap.equalsIgnoreCase("default")
            ? 0.2f
            : Float.parseFloat(gap);
        if (finalGap != to.getTranslation().y) {
            to.setTranslation(to.getTranslation().withY(finalGap));
        }
    }

    private static Component convertToComponent(Player self, Player sending, String line) {
        String formatted = line;

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            formatted = PlaceholderAPI.setRelationalPlaceholders(self, sending, formatted);
            formatted = PlaceholderAPI.setPlaceholders(self, formatted);
        }

        return MiniMessage.miniMessage().deserialize(formatted);
    }

}
