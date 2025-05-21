package com.mattmx.nametags.entity.trait;

import com.mattmx.nametags.entity.NameTagHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Trait {
    private @Nullable NameTagHolder nameTag;

    public void setNameTag(@NotNull NameTagHolder tag) {
        this.nameTag = tag;
    }

    public @NotNull NameTagHolder getTag() {
        assert nameTag != null;
        return nameTag;
    }

    public void onEnable() {
    }

    public void onDestroy() {
    }

}
