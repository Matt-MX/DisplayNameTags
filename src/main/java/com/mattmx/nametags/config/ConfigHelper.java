package com.mattmx.nametags.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConfigHelper {

    public static <T> @Nullable T takeIfPresent(
        @NotNull ConfigurationSection section,
        @NotNull String key,
        @NotNull Function<String, T> provider,
        @NotNull Consumer<T> take
    ) {
        if (section.get(key) == null) return null;

        Optional<T> optional = Optional.ofNullable(provider.apply(key));

        optional.ifPresent(take);

        return optional.orElse(null);
    }

    public static <T> @Nullable T takeIfPresentOrElse(
        @NotNull ConfigurationSection section,
        @NotNull String key,
        @NotNull Function<String, T> provider,
        @NotNull Consumer<T> take,
        @NotNull Runnable orElse
    ) {
        Optional<T> optional = Optional.ofNullable(provider.apply(key));

        optional.ifPresentOrElse(take, orElse);

        return optional.orElse(null);
    }

    public static <T extends Enum<T>> @Nullable T getEnumByNameOrNull(Class<T> enums, @NotNull String name) {
        return Arrays.stream(enums.getEnumConstants())
            .filter((e) -> e.name().equalsIgnoreCase(name))
            .findFirst().orElse(null);
    }

}
