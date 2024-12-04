package com.mattmx.nametags.extras.schema;

import org.jetbrains.annotations.NotNull;

public record CustomNameTag(
    @NotNull String text,
    int baseBackgroundColor,
    byte baseTextTransparency
) {

}
