package com.mattmx.nametags.entity.trait;

import com.mattmx.nametags.entity.NameTagEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Trait {
    private @Nullable NameTagEntity nameTag;

    public void setNameTag(@NotNull NameTagEntity tag) {
        this.nameTag = tag;
    }

    public @NotNull NameTagEntity getTag() {
        assert nameTag != null;
        return nameTag;
    }

    public void onDestroy() {

    }

}
