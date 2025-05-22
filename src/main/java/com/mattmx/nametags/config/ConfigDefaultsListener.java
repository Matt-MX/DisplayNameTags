package com.mattmx.nametags.config;

import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.entity.DefaultNameTagProvider;
import com.mattmx.nametags.entity.NameTagHolder;
import com.mattmx.nametags.entity.trait.RefreshTrait;
import com.mattmx.nametags.entity.trait.SneakTrait;
import com.mattmx.nametags.event.NameTagEntityCreateEvent;
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ConfigDefaultsListener implements Listener {
    private final @NotNull NameTags plugin;

    public ConfigDefaultsListener(@NotNull NameTags plugin) {
        this.plugin = plugin;

        final List<Map<?, ?>> defaultLines = defaultSection().getMapList("lines");
        final DefaultNameTagProvider provider = new DefaultNameTagProvider(new LinkedList<>());

        for (int i = 0; i < defaultLines.size(); i++) {
            final Map<?, ?> map = defaultLines.get(i);

            ConfigurationSection mapSection = new MemoryConfiguration();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                mapSection.set(entry.getKey().toString(), entry.getValue());
            }

            int finalI = i;
            provider.addLine((entity, meta) -> {
                meta.setUseDefaultBackground(false);
                meta.setTransformationInterpolationDuration(5);
                meta.setPositionRotationInterpolationDuration(5);
                TextDisplayMetaConfiguration.applyMeta(finalI, mapSection, meta);
            });
        }

        plugin.getEntityManager().setDefaultProvider(provider);
    }

    private ConfigurationSection defaultSection() {
        return plugin.getConfig().getConfigurationSection("defaults");
    }

    @EventHandler
    public void onCreate(@NotNull NameTagEntityCreateEvent event) {
        if (!(event.getNameTag().getBukkitEntity() instanceof Player)) return;

        // By default, we shouldn't notify until we have finished processing.
        event.getNameTag().modifyAll((meta) -> meta.setNotifyAboutChanges(false));

        long refreshMillis = plugin.getConfig().getLong("defaults.refresh-every", 50);

        if (refreshMillis == 0L) {
            return;
        }

        registerDefaultRefreshListener(event.getNameTag(), refreshMillis);
    }

    public void registerDefaultRefreshListener(@NotNull NameTagHolder tag, long refreshMillis) {
        Player player = (Player) tag.getBukkitEntity();

        tag.getTraits().getOrAddTrait(RefreshTrait.class, () ->
                RefreshTrait.ofMillis(
                        plugin,
                        refreshMillis,
                        this::updateNameTag
                )
        );
    }

    public void updateNameTag(@NotNull NameTagHolder holder) {
        try {
            if (!holder.getModificationLock().tryLock(20L, TimeUnit.MILLISECONDS)) {
                return;
            }
            Player player = (Player) holder.getBukkitEntity();

            // TODO we should cache this stuff
            holder.modifyAll((i, meta) -> {
                TextDisplayMetaConfiguration.applyMeta(i, defaultSection(), meta);
                TextDisplayMetaConfiguration.applyTextMeta(i, defaultSection(), meta, player);
            });

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
        } catch (Exception e) {

        } finally {
            holder.getModificationLock().unlock();
        }
    }

}
