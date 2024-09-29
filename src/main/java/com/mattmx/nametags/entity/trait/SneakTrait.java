package com.mattmx.nametags.entity.trait;

import com.mattmx.nametags.NameTags;
import org.bukkit.Color;

public class SneakTrait extends Trait {
    // FIXME RefreshTrait overrides this!
    private int previousOpacity = 0;

    public void updateSneak(boolean sneaking) {
        getTag().modify((meta) -> {
            Color color = Color.fromARGB(meta.getBackgroundColor());

            if (sneaking) {

                // If it's transparent then we shouldn't do anything really
                if (color.getAlpha() == 0) {
                    return;
                }

                previousOpacity = color.getAlpha();

                int sneakAmount = NameTags.getInstance()
                    .getConfig()
                    .getInt("sneak.opacity", 70);

                meta.setBackgroundColor(color.setAlpha(sneakAmount).asARGB());
            } else {
                meta.setBackgroundColor(color.setAlpha(previousOpacity).asARGB());
            }
        });
        getTag().getPassenger().refresh();
    }

}
