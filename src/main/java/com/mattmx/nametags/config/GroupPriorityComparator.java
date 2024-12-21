package com.mattmx.nametags.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Map;

public class GroupPriorityComparator {
    private static final @NotNull Comparator<Map.Entry<String, ConfigurationSection>> INSTANCE =
        Comparator.comparingInt((s) -> s.getValue().getInt("priority"));

    public static @NotNull Comparator<Map.Entry<String, ConfigurationSection>> get() {
        return INSTANCE;
    }
}
