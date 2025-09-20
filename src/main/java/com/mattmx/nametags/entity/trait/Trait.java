package com.mattmx.nametags.entity.trait;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Trait<T> {
    private @Nullable T holder;

    public void setHolder(@NotNull T tag) {
        this.holder = tag;
    }

    public @NotNull T getTag() {
        assert holder != null;
        return holder;
    }

    public void onEnable() {
    }

    public void onDestroy() {
    }

}
