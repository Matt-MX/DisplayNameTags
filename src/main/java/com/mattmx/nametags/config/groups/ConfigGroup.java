package com.mattmx.nametags.config.groups;

import com.mattmx.nametags.config.TextDisplayMetaConfiguration;
import com.mattmx.nametags.entity.NameTagEntity;
import com.mattmx.nametags.entity.NameTagHolder;
import lombok.Getter;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
public class ConfigGroup {

    private final String name;
    private final ConfigurationSection section;
    private final List<UpdatableLine> lines = new LinkedList<>();

    public ConfigGroup(String name, ConfigurationSection section) {
        this.name = name;
        this.section = section;

        // Check if any tags need creating or removing
        List<?> tagLines = section.getMapList("lines");

        // Hacky fix to tread Map<String, ?> as ConfigurationSections
        YamlConfiguration dummy = new YamlConfiguration();

        // Should be a list of Map<String, ?>
        for (int i = 0; i < tagLines.size(); i++) {
            Object entry = tagLines.get(i);
            // Convert this map entry (String:?) to a ConfigurationSection somehow
            try {
                Map<String, ?> map = (Map<String, ?>) entry;

                ConfigurationSection sub = dummy.createSection(String.valueOf(i), map);

                List<BoundConfigValue<?>> updates = ConfigurableEntry.bindEntries(sub);

                lines.add(new UpdatableLine(updates, sub));
            } catch (Exception ignored) {
            }
        }
    }

    public long getRefreshPeriodMillis() {
        return section.getLong(ConfigurableEntry.REFRESH_KEY, 500L);
    }

    public int getPriority() {
        return section.getInt(ConfigurableEntry.PRIORITY_KEY, 1);
    }

    public void apply(@NotNull NameTagHolder holder) {
        ensureEntities(holder);

        for (int i = 0; i < lines.size(); i++) {
            final NameTagEntity entity = holder.getEntities().get(i);
            final UpdatableLine line = lines.get(i);

            // Do not notify until we are done
            entity.notifyChanges(false);
            for (BoundConfigValue<?> update : line.updates) {
                update.entry().updateIfChanged(entity, update.value());
            }

            TextDisplayMeta meta = entity.getTextMeta();

            TextDisplayMetaConfiguration.applyTextMeta(line.section(), meta, holder.getOwner());
            TextDisplayMetaConfiguration.applyBackground(line.section(), meta);

            // Now we can emit changes
            entity.notifyChanges(true);
        }
    }

    /**
     * Internal method to ensure we have enough name tag entities.
     */
    private void ensureEntities(@NotNull NameTagHolder holder) {
        if (holder.getEntitiesList().size() == lines.size()) {
            return;
        }

        // Remove excess entities
        while (holder.getEntitiesList().size() > lines.size()) {
            final NameTagEntity last = holder.getEntitiesList().getLast();
            holder.removeEntity(last);
        }

        // Create needed entities
        while (holder.getEntitiesList().size() < lines.size()) {
            holder.createEntity();
        }
    }

    public @NotNull String getPermissionNode() {
        return "nametags.groups." + name;
    }

    public record UpdatableLine(List<BoundConfigValue<?>> updates, ConfigurationSection section) {
    }

}
