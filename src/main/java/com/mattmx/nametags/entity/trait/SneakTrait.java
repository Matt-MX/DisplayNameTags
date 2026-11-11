package com.mattmx.nametags.entity.trait;

import com.mattmx.nametags.NameTags;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

public class SneakTrait extends Trait {


    public void updateSneak(boolean sneaking) {
        getTag().getMeta().setSneaking(sneaking, getCustomOpacity());
        getTag().getPassenger().refreshMeta();
    }

    public @NotNull Color withCustomSneakOpacity(@NotNull Color previous) {
        return previous.getAlpha() == 0 ? previous : previous.setAlpha(getCustomOpacity());
    }

    public int getCustomOpacity() {
        return NameTags.getInstance().getSettings().sneakOpacity();
    }

    public boolean isSneaking() {
        return getTag().getMeta().isSneaking();
    }
}
