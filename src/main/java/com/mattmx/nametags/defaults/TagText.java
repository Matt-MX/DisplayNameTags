package com.mattmx.nametags.defaults;

import com.mattmx.nametags.config.Settings;
import com.mattmx.nametags.hook.PapiHook;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

final class TagText {
    private String @Nullable [] expanded;
    private @Nullable Component formatted;

    synchronized @Nullable Component build(@NotNull List<String> lines, @NotNull Player player, @NotNull Settings settings) {
        if (lines.isEmpty()) {
            return null;
        }
        String[] current = new String[lines.size()];
        for (int i = 0; i < current.length; i++) {
            current[i] = settings.placeholderApi() ? PapiHook.setPlaceholders(player, lines.get(i)) : lines.get(i);
        }
        if (formatted != null && Arrays.equals(current, expanded)) {
            return formatted;
        }
        Component text = null;
        for (String line : current) {
            Component component = settings.formatter().format(line);
            if (settings.removeEmptyLines() && component instanceof TextComponent textComponent && !TextComponent.IS_NOT_EMPTY.test(textComponent)) {
                continue;
            }
            text = text == null ? component : text.append(Component.newline()).append(component);
        }
        expanded = current;
        formatted = text;
        return text;
    }
}
