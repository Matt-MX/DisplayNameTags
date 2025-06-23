package com.mattmx.nametags.config;

import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.entity.NameTagEntity;
import com.mattmx.nametags.entity.trait.RefreshTrait;
import com.mattmx.nametags.entity.trait.SneakTrait;
import com.mattmx.nametags.event.NameTagEntityCreateEvent;
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class ConfigDefaultsListener implements Listener {
    private final @NotNull NameTags plugin;

    public ConfigDefaultsListener(@NotNull NameTags plugin) {
        this.plugin = plugin;

        NameTags.getInstance()
            .getEntityManager()
            .setDefaultProvider(((entity, meta) -> {
                meta.setUseDefaultBackground(false);
                
                // Version-specific interpolation handling
                String version = Bukkit.getMinecraftVersion();
                int defaultTransformation = 10;
                int defaultPositionRotation = 10;
                
                // For 1.21.5+ use higher interpolation values for smoother movement
                if (isVersion1215OrLater(version)) {
                    defaultTransformation = 15;
                    defaultPositionRotation = 15;
                }
                
                int transformationDuration = plugin.getConfig().getInt("options.interpolation.transformation-duration", defaultTransformation);
                int positionRotationDuration = plugin.getConfig().getInt("options.interpolation.position-rotation-duration", defaultPositionRotation);
                meta.setTransformationInterpolationDuration(transformationDuration);
                meta.setPositionRotationInterpolationDuration(positionRotationDuration);
                TextDisplayMetaConfiguration.applyMeta(defaultSection(), meta);
            }));
    }

    private ConfigurationSection defaultSection() {
        return plugin.getConfig().getConfigurationSection("defaults");
    }

    @EventHandler
    public void onCreate(@NotNull NameTagEntityCreateEvent event) {
        if (!(event.getNameTag().getBukkitEntity() instanceof Player player)) return;

        // By default, we shouldn't notify until we have finished processing.
        event.getNameTag()
            .getPassenger()
            .getEntityMeta()
            .setNotifyAboutChanges(false);

        long refreshMillis = plugin.getConfig().getLong("defaults.refresh-every", 50);

        if (refreshMillis == 0L) {
            return;
        }

        registerDefaultRefreshListener(event.getNameTag(), refreshMillis);
    }

    public void registerDefaultRefreshListener(@NotNull NameTagEntity tag, long refreshMillis) {
        Player player = (Player) tag.getBukkitEntity();

        tag.getTraits().getOrAddTrait(RefreshTrait.class, () ->
            RefreshTrait.ofMillis(
                plugin,
                refreshMillis,
                (entity) -> {
                    TextDisplayMetaConfiguration.applyMeta(defaultSection(), entity.getMeta());
                    TextDisplayMetaConfiguration.applyTextMeta(defaultSection(), entity.getMeta(), player);

                    // TODO we should cache this stuff
                    List<Map.Entry<String, ConfigurationSection>> groups = plugin.getGroups()
                        .entrySet()
                        .stream()
                        .filter((e) -> player.hasPermission(e.getKey()))
                        .sorted(GroupPriorityComparator.get())
                        .toList();

                    long recentRefreshEvery = plugin.getConfig().getLong("defaults.refresh-every", 50);
                    if (!groups.isEmpty()) {
                        Map.Entry<String, ConfigurationSection> highest = groups.getLast();

                        TextDisplayMetaConfiguration.applyMeta(highest.getValue(), entity.getMeta());
                        TextDisplayMetaConfiguration.applyTextMeta(highest.getValue(), entity.getMeta(), player);

                        long groupRefresh = highest.getValue().getLong("refresh-every", -1);
                        if (groupRefresh > 0) {
                            recentRefreshEvery = groupRefresh;
                        }
                    }

                    if (recentRefreshEvery != refreshMillis) {
                        entity.getTraits().removeTrait(RefreshTrait.class);
                        registerDefaultRefreshListener(tag, recentRefreshEvery);
                    }

                    if (entity.getMeta().getBillboardConstraints() == AbstractDisplayMeta.BillboardConstraints.CENTER) {
                        // Look passenger down to remove debug getting in the way
                        entity.getPassenger().rotateHead(0f, 90f);
                    }

                    // Preserve background color for sneaking
                    // Maybe we should introduce an `afterRefresh` callback?
                    entity.getTraits()
                        .getTrait(SneakTrait.class)
                        .ifPresent(SneakTrait::manuallyUpdateSneakingOpacity);

                    entity.updateVisibility();
                    entity.getPassenger().refresh();
                }
            )
        );
    }

    private boolean isVersion1215OrLater(String version) {
        try {
            // Parse version string like "1.21.5" or "1.21"
            String[] parts = version.split("\\.");
            if (parts.length >= 2) {
                int major = Integer.parseInt(parts[0]);
                int minor = Integer.parseInt(parts[1]);
                int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
                
                if (major > 1) return true;
                if (major == 1 && minor > 21) return true;
                if (major == 1 && minor == 21 && patch >= 5) return true;
            }
        } catch (NumberFormatException e) {
            // If we can't parse the version, assume it's a newer version
            return true;
        }
        return false;
    }

}
