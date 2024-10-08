package com.mattmx.nametags.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

public enum TextFormatter {
    MINI_MESSAGE(
        "minimessage",
        (line) -> MiniMessage.miniMessage().deserialize(line)
    ),
    LEGACY(
        "legacy",
        (line) -> LegacyComponentSerializer.legacyAmpersand().deserialize(line)
    )
    ;

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
