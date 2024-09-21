package com.mattmx.nametags.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

}
