package com.mattmx.nametags.config;

import com.mattmx.nametags.display.TagMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record TagGroup(
    @NotNull String permission,
    int priority,
    @NotNull TagStyle style,
    @NotNull List<String> lines,
    long refreshEvery
) {

    public void applyTo(@NotNull TagMeta meta, @Nullable TagStyle defaults) {
        if (defaults != null && defaults != style) {
            defaults.applyTo(meta);
        }
        style.applyTo(meta);
    }
}
