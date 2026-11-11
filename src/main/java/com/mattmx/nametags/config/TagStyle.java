package com.mattmx.nametags.config;

import com.github.retrooper.packetevents.util.Vector3f;
import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.display.TagMeta;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public record TagStyle(
    @Nullable Integer background,
    @Nullable TagMeta.BillboardConstraints billboard,
    @Nullable Boolean seeThrough,
    @Nullable Integer lineWidth,
    @Nullable Byte textOpacity,
    @Nullable Boolean textShadow,
    @Nullable Vector3f translation,
    @Nullable Float gap,
    @Nullable Vector3f scale,
    @Nullable Integer brightness,
    @Nullable Float shadowStrength,
    @Nullable Float shadowRadius,
    @Nullable Float viewRange
) {

    public static @NotNull TagStyle parse(@NotNull ConfigurationSection section) {
        ConfigurationSection shadow = section.getConfigurationSection("shadow");
        return new TagStyle(
            section.isSet("background") ? background(section.getString("background", "")) : null,
            section.isSet("billboard") ? billboard(section, section.getString("billboard", "")) : null,
            section.isSet("see-through") ? section.getBoolean("see-through") : null,
            section.isSet("line-width") ? section.getInt("line-width") : null,
            section.isSet("text-opacity") ? (byte) section.getInt("text-opacity") : null,
            section.isSet("text-shadow") ? section.getBoolean("text-shadow") : null,
            vector(section.getConfigurationSection("translate")),
            section.isSet("gap") ? gap(section.getString("gap", "default")) : null,
            vector(section.getConfigurationSection("scale")),
            section.isSet("brightness") ? section.getInt("brightness") : null,
            shadow == null ? null : (float) shadow.getDouble("strength"),
            shadow == null ? null : (float) shadow.getDouble("radius"),
            section.isSet("range") ? range(section.getString("range", "default")) : null
        );
    }

    public void applyTo(@NotNull TagMeta meta) {
        if (background != null) meta.setBackgroundColor(background);
        if (billboard != null) meta.setBillboardConstraints(billboard);
        if (seeThrough != null) meta.setSeeThrough(seeThrough);
        if (lineWidth != null) meta.setLineWidth(lineWidth);
        if (textOpacity != null) meta.setTextOpacity(textOpacity);
        if (textShadow != null) meta.setShadow(textShadow);
        if (translation != null) meta.setTranslation(translation);
        if (gap != null) meta.setTranslation(meta.getTranslation().withY(gap));
        if (scale != null) meta.setScale(scale);
        if (brightness != null) meta.setBrightnessOverride(brightness);
        if (shadowStrength != null) meta.setShadowStrength(shadowStrength);
        if (shadowRadius != null) meta.setShadowRadius(shadowRadius);
        if (viewRange != null) meta.setViewRange(viewRange);
    }

    private static int background(@NotNull String value) {
        if (value.equalsIgnoreCase("transparent")) {
            return NameTags.TRANSPARENT;
        }
        NamedTextColor named = NamedTextColor.NAMES.value(value);
        if (named != null) {
            return 0x40000000 | named.value();
        }
        if (value.startsWith("#")) {
            String hex = value.substring(1);
            if (hex.length() == 6) {
                return Color.fromARGB(Integer.parseInt(hex, 16)).setAlpha(0x40).asARGB();
            }
            if (hex.length() == 8) {
                return Color.fromARGB(Integer.parseInt(hex.substring(2), 16)).setAlpha(Integer.parseInt(hex.substring(0, 2), 16)).asARGB();
            }
            throw new IllegalArgumentException(String.format("Invalid hex string '#%s'!", hex));
        }
        return NameTags.TRANSPARENT;
    }

    private static @NotNull TagMeta.BillboardConstraints billboard(@NotNull ConfigurationSection section, @NotNull String value) {
        try {
            return TagMeta.BillboardConstraints.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException error) {
            throw new IllegalArgumentException("Unknown billboard type in section " + section.getCurrentPath() + " named " + value, error);
        }
    }

    private static @Nullable Vector3f vector(@Nullable ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        return new Vector3f((float) section.getDouble("x"), (float) section.getDouble("y"), (float) section.getDouble("z"));
    }

    private static float gap(@NotNull String value) {
        return value.equalsIgnoreCase("default") ? 0.2f : Float.parseFloat(value);
    }

    private static float range(@NotNull String value) {
        return value.equalsIgnoreCase("default") ? Bukkit.getSimulationDistance() * 16f : Float.parseFloat(value);
    }
}
