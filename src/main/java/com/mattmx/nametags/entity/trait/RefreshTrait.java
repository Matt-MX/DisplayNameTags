package com.mattmx.nametags.entity.trait;

import com.mattmx.nametags.entity.NameTagEntity;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RefreshTrait extends Trait {
    private final @NotNull ScheduledTask task;
    private boolean paused = false;

    public RefreshTrait(@NotNull JavaPlugin plugin, long period, TimeUnit unit, Consumer<NameTagEntity> update) {
        this.task = Bukkit.getAsyncScheduler()
            .runAtFixedRate(plugin, (task) -> {

                if (!this.isPaused()) {
                    update.accept(getTag());
                }

            }, 0L, period, unit);
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return this.paused;
    }

    @Override
    public void onDestroy() {
        setPaused(true);
        task.cancel();
    }

    public static @NotNull RefreshTrait ofMinutes(@NotNull JavaPlugin plugin, long minutes, Consumer<NameTagEntity> update) {
        return new RefreshTrait(plugin, minutes, TimeUnit.MINUTES, update);
    }

    public static @NotNull RefreshTrait ofSeconds(@NotNull JavaPlugin plugin, long seconds, Consumer<NameTagEntity> update) {
        return new RefreshTrait(plugin, seconds, TimeUnit.SECONDS, update);
    }

    public static @NotNull RefreshTrait ofMillis(@NotNull JavaPlugin plugin, long millis, Consumer<NameTagEntity> update) {
        return new RefreshTrait(plugin, millis, TimeUnit.MILLISECONDS, update);
    }

    public static @NotNull RefreshTrait ofTicks(@NotNull JavaPlugin plugin, long ticks, Consumer<NameTagEntity> update) {
        return ofMillis(plugin, ticks * 50, update);
    }

}
