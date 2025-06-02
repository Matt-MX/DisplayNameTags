package com.mattmx.nametags.config;

import com.mattmx.nametags.NameTags;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum TextFormatter {
    MINI_MESSAGE(
        "minimessage",
        (line) -> MiniMessage.miniMessage().deserialize(line)
    ),
    LEGACY(
        "legacy",
        (line) -> getLegacySerializer().deserialize(convertLegacyHex(line.replace(NameTags.LEGACY_CHAR, '&')))
    ),
    SMART(
        "smart",
        (line) -> {
            // First replace any legacy chars with &
            String mutableLine = convertLegacyHex(line.replace(NameTags.LEGACY_CHAR, '&'));

            // Convert legacy to modern formatting
            mutableLine = convertLegacyHexToMiniMessage(mutableLine);
            mutableLine = mutableLine
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&k", "<obf>")
                .replace("&l", "<b>")
                .replace("&m", "<st>")
                .replace("&n", "<u>")
                .replace("&o", "<i>")
                .replace("&r", "<reset>");

            return MINI_MESSAGE.format(mutableLine);
        }
    );

    // Converts legacy hex format &x&9&0&0&c&3&f -> &#900c3f modern hex format
    // https://github.com/Matt-MX/DisplayNameTags/issues/32#issuecomment-2509403581
    private static final Pattern LEGACY_HEX_PATTERN = Pattern.compile("&x(&[0-9a-fA-F]){6}");

    public static String convertLegacyHex(String input) {
        Matcher matcher = LEGACY_HEX_PATTERN.matcher(input);

        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String legacyHex = matcher.group();
            // Extract hex digits from the legacy format
            String hexColor = legacyHex.replace("&x", "")
                .replace("&", "");
            // Replace with modern format
            String modernHex = "&#" + hexColor;
            matcher.appendReplacement(result, modernHex);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Converts Minecraft legacy hex color codes (&#RRGGBB) to MiniMessage format (<#RRGGBB>).
     *
     * @param legacyText The input string with legacy color codes.
     * @return The converted string in MiniMessage format.
     */
    public static String convertLegacyHexToMiniMessage(@NotNull String legacyText) {
        if (legacyText.isEmpty()) {
            return legacyText;
        }

        // Regex to match legacy hex color codes (&# followed by 6 hexadecimal characters)
        Pattern legacyHexPattern = Pattern.compile("&#([0-9a-fA-F]{6})");
        Matcher matcher = legacyHexPattern.matcher(legacyText);

        StringBuilder convertedText = new StringBuilder();

        while (matcher.find()) {
            // Extract the hex color code (RRGGBB)
            String hexColor = matcher.group(1);

            // Replace with MiniMessage format
            matcher.appendReplacement(convertedText, "<#" + hexColor + ">");
        }

        // Append the rest of the text
        matcher.appendTail(convertedText);

        return convertedText.toString();
    }

    private static final LegacyComponentSerializer legacy = LegacyComponentSerializer.builder()
        .character('&')
        .hexCharacter('#')
        .hexColors()
        .build();

    public static @NotNull LegacyComponentSerializer getLegacySerializer() {
        return legacy;
    }

    private final @NotNull String identifier;
    private final @NotNull Function<String, Component> formatter;

    TextFormatter(@NotNull String identifier, @NotNull Function<String, Component> formatter) {
        this.identifier = identifier;
        this.formatter = formatter;
    }

    public @NotNull Component format(@NotNull String line) {
        return formatter.apply(line);
    }

    public static @NotNull Optional<TextFormatter> getById(@NotNull String identifier) {
        return Arrays.stream(values()).filter((f) -> f.identifier.equalsIgnoreCase(identifier)).findFirst();
    }
}
