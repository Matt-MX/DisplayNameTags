package com.mattmx.nametags.config;

import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.entity.NameTagHolder;
import com.mattmx.nametags.entity.trait.Trait;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public class DefaultsTrait extends Trait<NameTagHolder> {
    private @Nullable ScheduledTask repeating;

    @Override
    public void onEnable() {
        // Create repeating task to update the tag members
        scheduleTask();
    }

    @Override
    public void onDestroy() {
        if (this.repeating != null) {
            this.repeating.cancel();
        }
    }

    public void scheduleTask() {
        // Cancel current (scheduled) execution
        if (this.repeating != null) {
            this.repeating.cancel();
        }

        this.repeating = Bukkit.getAsyncScheduler().runAtFixedRate(
                NameTags.getInstance(),
                (t) -> update(),
                1L, 1L, TimeUnit.MILLISECONDS
        );
    }

    public void update() {

    }

    public void updatePermissions() {

    }
}
