package com.mattmx.nametags.config;

import com.github.retrooper.packetevents.util.Vector3f;
import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.hook.PapiHook;
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

public class TextDisplayMetaConfiguration {

    public static boolean applyTextMeta(@NotNull ConfigurationSection section, @NotNull TextDisplayMeta to, @NotNull Player self, @NotNull Player sender) {
        Stream<Component> stream = section.getStringList("text")
            .stream()
            .map((line) -> convertToComponent(self, sender, line));

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

    public static void applyMeta(@NotNull ConfigurationSection section, @NotNull TextDisplayMeta to) {

        ConfigHelper.takeIfPresent(section, "background", section::getString, (backgroundColor) -> {
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

            to.setBackgroundColor(background);
        });

        ConfigHelper.takeIfPresent(section, "billboard", section::getString, (billboardString) -> {
            AbstractDisplayMeta.BillboardConstraints billboard = ConfigHelper.getEnumByNameOrNull(
                AbstractDisplayMeta.BillboardConstraints.class,
                billboardString.toLowerCase(Locale.ROOT)
            );

            Objects.requireNonNull(billboard, "Unknown billboard type in section " + section.getCurrentPath() + " named " + billboardString);

            if (billboard != to.getBillboardConstraints()) {
                to.setBillboardConstraints(billboard);
            }
        });

        ConfigHelper.takeIfPresent(section, "see-through", section::getBoolean, (seeThrough) -> {
            if (to.isSeeThrough() != seeThrough) {
                to.setSeeThrough(seeThrough);
            }
        });

        ConfigHelper.takeIfPresent(section, "line-width", section::getInt, (lineWidth) -> {
            if (to.getLineWidth() != lineWidth) {
                to.setLineWidth(lineWidth);
            }
        });

        ConfigHelper.takeIfPresent(section, "text-opacity", section::getInt, (opacity) -> {
            byte byteValue = opacity.byteValue();
            if (to.getTextOpacity() != byteValue) {
                to.setTextOpacity(byteValue);
            }
        });

        ConfigHelper.takeIfPresent(section, "text-shadow", section::getBoolean, (shadow) -> {
            if (to.isShadow() != shadow) {
                to.setShadow(shadow);
            }
        });

        ConfigHelper.takeIfPresent(section, "translate", section::getConfigurationSection, (vector) -> {
            double dx = vector.getDouble("x");
            double dy = vector.getDouble("y");
            double dz = vector.getDouble("z");

            Vector3f vec = new Vector3f((float) dx, (float) dy, (float) dz);
            if (!Objects.equals(to.getTranslation(), vec)) {
                to.setTranslation(vec);
            }
        });

        ConfigHelper.takeIfPresent(section, "gap", section::getString, (gap) -> {
            float finalGap = gap.equalsIgnoreCase("default")
                ? 0.2f
                : Float.parseFloat(gap);
            if (finalGap != to.getTranslation().y) {
                to.setTranslation(to.getTranslation().withY(finalGap));
            }
        });

        ConfigHelper.takeIfPresent(section, "scale", section::getConfigurationSection, (vector) -> {
            double dx = vector.getDouble("x");
            double dy = vector.getDouble("y");
            double dz = vector.getDouble("z");

            Vector3f vec = new Vector3f((float) dx, (float) dy, (float) dz);
            if (!Objects.equals(to.getScale(), vec)) {
                to.setScale(vec);
            }
        });

        ConfigHelper.takeIfPresent(section, "brightness", section::getInt, (brightness) -> {
            if (to.getBrightnessOverride() != brightness) {
                to.setBrightnessOverride(brightness);
            }
        });

        ConfigHelper.takeIfPresent(section, "shadow", section::getConfigurationSection, (shadow) -> {
            float strength = (float) shadow.getDouble("strength");
            float radius = (float) shadow.getDouble("radius");

            if (to.getShadowStrength() != strength) {
                to.setShadowStrength(strength);
            }
            if (to.getShadowRadius() != radius) {
                to.setShadowRadius(radius);
            }

        });

        ConfigHelper.takeIfPresent(section, "range", section::getString, (range) -> {
            float finalRange = range.equalsIgnoreCase("default")
                ? (Bukkit.getSimulationDistance() * 16f)
                : Float.parseFloat(range);

            if (finalRange != to.getViewRange()) {
                to.setViewRange(finalRange);
            }
        });

        // TODO(matt): This function needs access to the WrapperEntity
        String yaw = section.getString("yaw");
        String pitch = section.getString("pitch");
    }

    private static Component convertToComponent(Player self, Player sending, String line) {
        String formatted = line;

        formatted = PapiHook.setPlaceholders(self, sending, formatted);

        return NameTags.getInstance()
            .getFormatter()
            .format(formatted);
    }

}
