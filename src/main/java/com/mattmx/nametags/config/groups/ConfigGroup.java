package com.mattmx.nametags.config.groups;

import com.mattmx.nametags.entity.NameTagHolder;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ConfigGroup {

    private final String name;
    private final Map<String, ConfigurableEntry<?, ?>> entries;

    public ConfigGroup(String name, ConfigurationSection section) {
        this.name = name;
        this.entries = ConfigurableEntry.filterEntries(section);
    }

    public void apply(@NotNull NameTagHolder holder) {
        
    }

}
