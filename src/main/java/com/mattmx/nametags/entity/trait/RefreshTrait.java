package com.mattmx.nametags.entity.trait;

import com.mattmx.nametags.entity.NameTagEntity;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class RefreshTrait extends Trait {
    private final JavaPlugin plugin;
    private final long period;
    private final TimeUnit unit;
    private final Consumer<NameTagEntity> update;
    private final AtomicBoolean forced = new AtomicBoolean();
    private final Object running = new Object();
    private volatile @Nullable ScheduledTask task;
    private volatile boolean paused;

    public RefreshTrait(@NotNull JavaPlugin plugin, long period, @NotNull TimeUnit unit, @NotNull Consumer<NameTagEntity> update) {
        this.plugin = plugin;
        this.period = period;
        this.unit = unit;
        this.update = update;
    }

    @Override
    public void onEnable() {
        task = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, scheduled -> run(), 0L, period, unit);
    }

    public @NotNull Consumer<NameTagEntity> getUpdate() {
        return update;
    }

    public long getPeriodMillis() {
        return unit.toMillis(period);
    }

    public void forceUpdate() {
        if (forced.compareAndSet(false, true)) {
            Bukkit.getAsyncScheduler().runNow(plugin, scheduled -> {
                forced.set(false);
                run();
            });
        }
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    @Override
    public void onDestroy() {
        paused = true;
        ScheduledTask current = task;
        if (current != null) {
            current.cancel();
        }
    }

    private void run() {
        synchronized (running) {
            if (paused || !getTag().getPassenger().isSpawned()) {
                return;
            }
            update.accept(getTag());
        }
    }

    public static @NotNull RefreshTrait ofMinutes(@NotNull JavaPlugin plugin, long minutes, @NotNull Consumer<NameTagEntity> update) {
        return new RefreshTrait(plugin, minutes, TimeUnit.MINUTES, update);
    }

    public static @NotNull RefreshTrait ofSeconds(@NotNull JavaPlugin plugin, long seconds, @NotNull Consumer<NameTagEntity> update) {
        return new RefreshTrait(plugin, seconds, TimeUnit.SECONDS, update);
    }

    public static @NotNull RefreshTrait ofMillis(@NotNull JavaPlugin plugin, long millis, @NotNull Consumer<NameTagEntity> update) {
        return new RefreshTrait(plugin, millis, TimeUnit.MILLISECONDS, update);
    }

    public static @NotNull RefreshTrait ofTicks(@NotNull JavaPlugin plugin, long ticks, @NotNull Consumer<NameTagEntity> update) {
        return ofMillis(plugin, ticks * 50, update);
    }
}
