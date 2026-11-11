package com.mattmx.nametags.display;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentIteratorType;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;

final class TextLines {

    private TextLines() {
    }

    static @NotNull TextComponent withoutEmptyLines(@NotNull TextComponent component) {
        TextComponent.Builder result = Component.text();
        TextComponent.Builder line = Component.text();
        boolean lineHasText = false;
        boolean first = true;
        for (Component part : component.iterable(ComponentIteratorType.DEPTH_FIRST)) {
            if (!(part instanceof TextComponent text)) {
                line.append(part);
                continue;
            }
            String[] segments = text.content().split("\n", -1);
            for (int i = 0; i < segments.length; i++) {
                String segment = segments[i];
                if (!segment.isEmpty()) {
                    line.append(Component.text(segment).style(text.style()));
                    lineHasText |= !segment.isBlank();
                }
                if (i < segments.length - 1) {
                    if (lineHasText) {
                        if (!first) {
                            result.append(Component.newline());
                        }
                        result.append(line.build());
                        first = false;
                    }
                    line = Component.text();
                    lineHasText = false;
                }
            }
        }
        if (lineHasText) {
            if (!first) {
                result.append(Component.newline());
            }
            result.append(line.build());
        }
        return result.build();
    }
}
