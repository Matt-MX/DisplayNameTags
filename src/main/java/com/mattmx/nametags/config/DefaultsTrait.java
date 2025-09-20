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

import java.util.List;
import java.util.concurrent.TimeUnit;

public class DefaultsTrait extends Trait<NameTagHolder> {
    // TODO: Change to a task per entity
    private @Nullable ScheduledTask repeating;
    private ConfigGroup group;

    @Override
    public void onEnable() {
        updatePermissions();

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
        // Should be the base then the group (nothing else)
        group.apply(getTag());

        // Here we should update the text and bg colors (lines should be the same length now)
        for (int i = 0; i < group.getLines().size(); i++) {
            final NameTagEntity entity = getTag().getEntities().get(i);
            final ConfigGroup.UpdatableLine line = group.getLines().get(i);

            TextDisplayMeta meta = entity.getTextMeta();

            TextDisplayMetaConfiguration.applyTextMeta(line.section(), meta, getTag().getOwner());
            TextDisplayMetaConfiguration.applyBackground(line.section(), meta);

            // Now we can emit changes
            entity.notifyChanges(true);
        }
    }

    public void updatePermissions() {
        final ConfigGroup previousGroup = this.group;

        List<ConfigGroup> groups = NameTags.getInstance()
            .getGroups()
            .stream()
            .filter((e) -> getTag().getOwner().hasPermission(e.getName()))
            .sorted(GroupPriorityComparator.get())
            .toList();

        if (groups.isEmpty()) {
            this.group = NameTags.getInstance().getDefaultGroup();
        } else {
            this.group = groups.getFirst();
        }

        // If the group changed, then restart the repeating task
        if (this.group != previousGroup) {
            scheduleTask();
        }
    }
}
