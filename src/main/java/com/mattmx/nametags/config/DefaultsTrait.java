package com.mattmx.nametags.config;

import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.config.groups.ConfigGroup;
import com.mattmx.nametags.entity.NameTagEntity;
import com.mattmx.nametags.entity.NameTagHolder;
import com.mattmx.nametags.entity.trait.Trait;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DefaultsTrait extends Trait<NameTagHolder> {
    // TODO: Change to a task per entity
    private @Nullable ScheduledTask repeating;
    private ConfigGroup group;

    @Override
    public void onEnable() {
        updateGroup();

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

        long refresh = group.getRefreshPeriodMillis();
        this.repeating = Bukkit.getAsyncScheduler().runAtFixedRate(
            NameTags.getInstance(),
            (t) -> update(),
            1L, refresh, TimeUnit.MILLISECONDS
        );
    }

    public void update() {
        group.apply(getOwner());
    }

    public void updateGroup() {
        final ConfigGroup previousGroup = this.group;

        ConfigGroup newGroup = null;
        int groupPriority = Integer.MIN_VALUE;

        DefaultsHook hook = Objects.requireNonNull(NameTags.getInstance().getDefaults());
        for (ConfigGroup group : hook.getGroups()) {
            boolean hasPermission = getOwner().getOwner().hasPermission(group.getPermissionNode());

            if (hasPermission && group.getPriority() > groupPriority) {
                newGroup = group;
                groupPriority = group.getPriority();
            }
        }

        if (newGroup == null) {
            this.group = hook.getDefaultGroup();
        } else {
            this.group = newGroup;
        }

        // If the group changed, then restart the repeating task
        if (this.group != previousGroup) {
            scheduleTask();
        }
    }
}
