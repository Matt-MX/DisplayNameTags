package com.mattmx.nametags.entity;

import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.entity.trait.Trait;
import com.mattmx.nametags.event.NameTagEntityCreateEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class TestImplTrait implements Listener {

    @EventHandler
    public void onNameTagCreate(@NotNull NameTagEntityCreateEvent event) {
        event.getNameTag()
            .getTraits()
            .getOrAddTrait(RefreshTrait.class, RefreshTrait::new);
    }

    static class RefreshTrait extends Trait {
        private int i = 0;
        private boolean cancel = false;
        private final @NotNull BukkitTask task = Bukkit.getScheduler()
            .runTaskTimerAsynchronously(NameTags.getInstance(), () -> {
                if (cancel) return;

                getTag().modify((meta) -> {
                    meta.setText(getTag().getBukkitEntity()
                        .name()
                        .color(i++ % 2 == 0 ? NamedTextColor.RED : NamedTextColor.GREEN));
                });

            }, 0L, 20L);

        @Override
        public void onDestroy() {
            cancel = true;
            task.cancel();
        }
    }

}
