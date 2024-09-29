package com.mattmx.nametags.config;

import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.entity.trait.RefreshTrait;
import com.mattmx.nametags.entity.trait.SneakTrait;
import com.mattmx.nametags.event.NameTagEntityCreateEvent;
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class ConfigDefaultsListener implements Listener {
    private final @NotNull NameTags plugin;

    public ConfigDefaultsListener(@NotNull NameTags plugin) {
        this.plugin = plugin;

        NameTags.getInstance()
            .getEntityManager()
            .setDefaultProvider(((entity, meta) -> {
                meta.setUseDefaultBackground(false);
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

        event.getNameTag()
            .getTraits()
            .getOrAddTrait(RefreshTrait.class, () ->
                RefreshTrait.ofMillis(
                    plugin,
                    refreshMillis,
                    (entity) -> {
                        TextDisplayMetaConfiguration.applyMeta(defaultSection(), entity.getMeta());
                        TextDisplayMetaConfiguration.applyTextMeta(defaultSection(), entity.getMeta(), player, player);

                        // TODO we should cache this stuff
                        plugin.getGroups()
                            .entrySet()
                            .stream()
                            .filter((e) -> player.hasPermission(e.getKey()))
                            .forEach((e) -> {
                                TextDisplayMetaConfiguration.applyMeta(e.getValue(), entity.getMeta());
                                TextDisplayMetaConfiguration.applyTextMeta(e.getValue(), entity.getMeta(), player, player);
                            });


                        if (entity.getMeta().getBillboardConstraints() == AbstractDisplayMeta.BillboardConstraints.CENTER) {
                            // Look passenger down to remove debug getting in the way
                            entity.getPassenger().rotateHead(0f, 90f);
                        }

                        // Preserve background color for sneaking
                        // Maybe we should introduce an `afterRefresh` callback?
                        entity.getTraits()
                            .getTrait(SneakTrait.class)
                            .ifPresent((sneak) -> {
                                if (!sneak.isSneaking()) return;

                                entity.modify((tag) -> {
                                    Color currentColor = Color.fromARGB(tag.getBackgroundColor());
                                    tag.setBackgroundColor(sneak.withCustomSneakOpacity(currentColor).asARGB());
                                });
                            });

                        entity.updateVisibility();
                        entity.getPassenger().refresh();
                    }
                )
            );
    }

}
