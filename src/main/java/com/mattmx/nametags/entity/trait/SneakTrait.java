package com.mattmx.nametags.entity.trait;

import com.mattmx.nametags.NameTags;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

public class SneakTrait extends Trait {
    private int previousBackgroundOpacity = 0;
    private int previousTextOpacity = Byte.MAX_VALUE;
    private boolean isSneaking = false;

    public void updateSneak(boolean sneaking) {
        this.isSneaking = sneaking;
        getTag().modify((meta) -> {
            Color color = Color.fromARGB(meta.getBackgroundColor());

            if (sneaking) {

                // If it's transparent then we shouldn't do anything really
                if (color.getAlpha() == 0) {
                    return;
                }

                previousBackgroundOpacity = color.getAlpha();
                previousTextOpacity = meta.getTextOpacity();

                // Not sure if this is vanilla behavior? Does only text opacity change??
                meta.setBackgroundColor(withCustomSneakOpacity(color).asARGB());
                meta.setTextOpacity((byte) getCustomOpacity());
            } else {
                meta.setBackgroundColor(color.setAlpha(previousBackgroundOpacity).asARGB());
                meta.setTextOpacity((byte) previousTextOpacity);
            }
        });
        getTag().getPassenger().refresh();
    }

    public Color withCustomSneakOpacity(@NotNull Color previous) {
        return previous.setAlpha(getCustomOpacity());
    }

    public int getCustomOpacity() {
        return NameTags.getInstance()
            .getConfig()
            .getInt("sneak.opacity", 70);
    }

    public boolean isSneaking() {
        return isSneaking;
    }
}
