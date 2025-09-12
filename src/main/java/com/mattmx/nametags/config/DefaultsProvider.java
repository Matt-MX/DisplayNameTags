package com.mattmx.nametags.config;

import com.mattmx.nametags.NameTagHolderDefaults;
import com.mattmx.nametags.entity.NameTagHolder;
import org.jetbrains.annotations.NotNull;

public class DefaultsProvider implements NameTagHolderDefaults {
    private final DefaultsHook defaults;

    public DefaultsProvider(DefaultsHook defaults) {
        this.defaults = defaults;
    }

    @Override
    public void applyDefaults(@NotNull NameTagHolder holder) {

    }

}
