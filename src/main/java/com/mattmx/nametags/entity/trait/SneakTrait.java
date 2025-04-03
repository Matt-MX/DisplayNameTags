package com.mattmx.nametags.entity.trait;

import com.mattmx.nametags.NameTags;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

public class SneakTrait extends Trait {
    private int previousBackgroundOpacity = 0;
    private byte previousTextOpacity = Byte.MAX_VALUE;
    private boolean isSneaking = false;

    public void manuallyUpdateSneakingOpacity() {
        if (!isSneaking()) return;

        getTag().modify((tag) -> {
            Color currentColor = Color.fromARGB(tag.getBackgroundColor());
            tag.setBackgroundColor(withCustomSneakOpacity(currentColor).asARGB());
            tag.setTextOpacity((byte) getCustomOpacity());
        });
    }

    public void updateSneak(boolean sneaking) {
        this.isSneaking = sneaking;
        getTag().modify((meta) -> {
            Color color = Color.fromARGB(meta.getBackgroundColor());

            if (sneaking) {
                previousBackgroundOpacity = color.getAlpha();
                previousTextOpacity = meta.getTextOpacity();

                // Not sure if this is vanilla behavior? Does only text opacity change??
                meta.setBackgroundColor(withCustomSneakOpacity(color).asARGB());
                meta.setTextOpacity((byte) getCustomOpacity());
            } else {
                meta.setBackgroundColor(color.setAlpha(previousBackgroundOpacity).asARGB());
                meta.setTextOpacity(previousTextOpacity);
            }
        });
        getTag().getPassenger().refresh();
    }

    public Color withCustomSneakOpacity(@NotNull Color previous) {
        if (previous.getAlpha() == 0) {
            return previous;
        }

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
