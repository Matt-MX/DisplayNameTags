package com.mattmx.nametags;

import com.github.retrooper.packetevents.util.Vector3f;
import com.mattmx.nametags.entity.NameTagEntity;
import com.mattmx.nametags.entity.NameTagHolder;
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface NameTagHolderDefaults {

    void applyDefaults(@NotNull NameTagHolder holder);

    static NameTagHolderDefaults vanilla() {
        return (holder) -> {
            NameTagEntity entity = holder.createEntity();
            entity.updateTextMeta((meta) -> {
                meta.setText(holder.getOwner().name());
                meta.setTranslation(new Vector3f(0f, 0.2f, 0f));
                meta.setBillboardConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
                meta.setViewRange(50f);
            });
        };
    }

}
