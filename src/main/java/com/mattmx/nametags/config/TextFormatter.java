package com.mattmx.nametags.config;

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
        (line) -> getLegacySerializer().deserialize(convertLegacyHex(line))
    )
    ;

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
