package com.mattmx.nametags.config;

import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.entity.trait.RefreshTrait;
import com.mattmx.nametags.event.NameTagEntityCreateEvent;
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

        event.getNameTag()
            .getTraits()
            .getOrAddTrait(RefreshTrait.class, () ->
                RefreshTrait.ofSeconds(
                    NameTags.getInstance(),
                    1L,
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

                        entity.getPassenger().refresh();
                    }
                )
            );
    }

}
