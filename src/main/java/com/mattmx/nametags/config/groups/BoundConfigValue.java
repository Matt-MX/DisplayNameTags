package com.mattmx.nametags.config.groups;

import me.tofaa.entitylib.meta.display.AbstractDisplayMeta;
import org.jetbrains.annotations.NotNull;

public record BoundConfigValue<E extends AbstractDisplayMeta>(
    @NotNull Object value,
    @NotNull ConfigurableEntry<?, E> entry
) {}
