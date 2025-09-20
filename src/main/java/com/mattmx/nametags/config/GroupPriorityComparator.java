package com.mattmx.nametags.config;

import com.mattmx.nametags.config.groups.ConfigGroup;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Map;

public class GroupPriorityComparator {
    private static final @NotNull Comparator<ConfigGroup> INSTANCE = Comparator.comparingInt(ConfigGroup::getPriority);

    public static @NotNull Comparator<ConfigGroup> get() {
        return INSTANCE;
    }
}
