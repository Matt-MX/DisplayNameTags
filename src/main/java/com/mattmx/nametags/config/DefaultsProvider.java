package com.mattmx.nametags.config;

import com.mattmx.nametags.NameTagHolderDefaults;
import com.mattmx.nametags.config.groups.ConfigGroup;
import com.mattmx.nametags.entity.NameTagHolder;
import org.jetbrains.annotations.NotNull;

public class DefaultsProvider implements NameTagHolderDefaults {
    private final ConfigGroup defaultGroup;

    public DefaultsProvider(ConfigGroup defaultGroup) {
        this.defaultGroup = defaultGroup;
    }

    @Override
    public void applyDefaults(@NotNull NameTagHolder holder) {
        this.defaultGroup.apply(holder);
    }

}
