package com.mattmx.nametags.config;

import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.config.groups.ConfigGroup;
import com.mattmx.nametags.entity.NameTagEntity;
import com.mattmx.nametags.entity.NameTagHolder;
import com.mattmx.nametags.entity.trait.Trait;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class DefaultsTrait extends Trait<NameTagHolder> {
    private final Map<Integer, ScheduledTask> repeatingTasks = new ConcurrentHashMap<>();
    private ConfigGroup group;

    @Override
    public void onEnable() {
        updateGroup();

        // Create repeating task to update the tag members
        scheduleRepeatingTasks();
    }

    @Override
    public void onDestroy() {
        if (!this.repeatingTasks.isEmpty()) {
            this.repeatingTasks.values().forEach(ScheduledTask::cancel);
            this.repeatingTasks.clear();
        }
    }

    public void scheduleRepeatingTasks() {
        // Cancel current (scheduled) execution
        if (!this.repeatingTasks.isEmpty()) {
            this.repeatingTasks.values().forEach(ScheduledTask::cancel);
            this.repeatingTasks.clear();
        }

        for (int i = 0; i < group.getLines().size(); i++) {
            final NameTagEntity entity = getOwner().getEntities().get(i);
            final ConfigGroup.UpdatableLine line = group.getLines().get(i);

            line.getRefreshPeriod()
                .or(group::getDefaultRefreshPeriod)
                .ifPresent((refreshMillis) -> {
                    ScheduledTask task = Bukkit.getAsyncScheduler().runAtFixedRate(
                        NameTags.getInstance(),
                        (t) -> update(),
                        0L, refreshMillis, TimeUnit.MILLISECONDS
                    );

                    repeatingTasks.put(entity.getWrapperEntity().getEntityId(), task);
                });
        }
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
            scheduleRepeatingTasks();
        }
    }
}
