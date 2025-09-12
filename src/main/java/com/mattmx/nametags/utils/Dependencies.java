package com.mattmx.nametags.utils;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class Dependencies {
    public static final Dependency LUCKPERMS = new Dependency("LuckPerms");

    public record Dependency(@NotNull String name) {
        public boolean isPresent() {
            return Bukkit.getPluginManager().isPluginEnabled(name);
        }
    }

}
