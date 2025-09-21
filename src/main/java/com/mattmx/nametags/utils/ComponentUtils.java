package com.mattmx.nametags.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentIteratorType;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;

public class ComponentUtils {

    public static boolean startsWith(@NotNull TextComponent checking, @NotNull TextComponent test) {
        return checking.contains(test, (a, b) -> {
            if (!(a instanceof TextComponent aText) || !(b instanceof TextComponent bText)) {
                return false;
            }

            return (aText).content().startsWith((bText).content());
        });
    }

    public static Component removeEmptyLines(@NotNull TextComponent component) {
        return component.replaceText(builder -> builder
            .match("\n\\s*\n")
            .replacement("")
        );
    }

    public static TextComponent removeEmptyLines0(@NotNull TextComponent component) {
        List<TextComponent> lines = splitByNewLine(component);

        final TextComponent.Builder builder = Component.text();
        for (TextComponent line : lines) {
            // If it matches an empty line regex do not include it
            if (line.content().matches("\\s*")) {
                continue;
            }

            builder.append(line);
        }

        return builder.build();
    }

    public static List<TextComponent> splitByNewLine(@NotNull TextComponent component) {
        List<TextComponent> lines = new ArrayList<>();
        final TextComponent.Builder[] currentLine = {Component.text()};

        Spliterator<Component> spliterator = component.spliterator(ComponentIteratorType.DEPTH_FIRST);
        spliterator.forEachRemaining(part -> {
            if (!(part instanceof TextComponent textPart)) {
                currentLine[0].append(part);
                return;
            }

            String content = textPart.content();
            String[] segments = content.split("\n", -1); // keep empty segments

            for (int i = 0; i < segments.length; i++) {
                String segment = segments[i];
                if (!segment.isEmpty()) {
                    currentLine[0].append(Component.text(segment).style(part.style()));
                }

                // If not the last segment, we hit a \n and should start a new line
                if (i < segments.length - 1) {
                    TextComponent built = currentLine[0].build();
                    if (!built.equals(Component.empty())) {
                        lines.add(built);
                    }
                    currentLine[0] = Component.text(); // start a new line
                }
            }
        });

        return lines;
    }
}
