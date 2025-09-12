package com.mattmx.nametags.utils;

import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;

public class ComponentUtils {

    public static boolean contains(@NotNull TextComponent checking, @NotNull TextComponent test) {
        return checking.contains(test, (a, b) -> {
            if (!(a instanceof TextComponent aText) || !(b instanceof TextComponent bText)) {
                return false;
            }

            return aText.content().contains(bText.content());
        });
    }

}
