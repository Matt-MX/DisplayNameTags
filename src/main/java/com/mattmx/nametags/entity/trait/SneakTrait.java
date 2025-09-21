package com.mattmx.nametags.entity.trait;

import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.entity.NameTagEntity;
import lombok.Getter;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

public class SneakTrait extends Trait<NameTagEntity> {
    private final byte sneakingOpacity = (byte) NameTags.getInstance()
        .getConfig()
        .getInt("sneak.opacity", 70);

    private int preSneakingBackgroundColor = 0;
    private byte preSneakingTextOpacity = 0;

    @Getter
    private boolean isCurrentlySneaking = false;

    public void setSneaking(boolean sneaking) {
        if (this.isCurrentlySneaking == sneaking) {
            return;
        }

        this.isCurrentlySneaking = sneaking;

        NameTagEntity entity = getOwner();

        entity.notifyChanges(false);

        TextDisplayMeta meta = entity.getTextMeta();

        int backgroundColorInt = meta.getBackgroundColor();
        Color backgroundColor = Color.fromARGB(backgroundColorInt);

        if (isCurrentlySneaking) {
            this.preSneakingBackgroundColor = backgroundColorInt;
            this.preSneakingTextOpacity = meta.getTextOpacity();

            meta.setBackgroundColor(withCustomSneakOpacity(backgroundColor).asARGB());
            meta.setTextOpacity(sneakingOpacity);
        } else {
            meta.setBackgroundColor(this.preSneakingBackgroundColor);
            meta.setTextOpacity(this.preSneakingTextOpacity);
        }

        entity.notifyChanges(true);
    }

    public Color withCustomSneakOpacity(@NotNull Color previous) {
        if (previous.getAlpha() == 0) {
            return previous;
        }

        return previous.setAlpha(sneakingOpacity);
    }
}
