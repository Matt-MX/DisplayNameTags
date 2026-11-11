package com.mattmx.nametags.entity.trait;

import com.mattmx.nametags.entity.NameTagEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Trait {
    private volatile @Nullable NameTagEntity nameTag;

    public void setNameTag(@NotNull NameTagEntity tag) {
        this.nameTag = tag;
    }

    public @NotNull NameTagEntity getTag() {
        NameTagEntity tag = nameTag;
        if (tag == null) {
            throw new IllegalStateException("Trait used before it was added to a tag");
        }
        return tag;
    }

    public void onEnable() {
    }

    public void onDestroy() {
    }

}
