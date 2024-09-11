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
            .setDefaultProvider(((entity, meta) -> TextDisplayMetaConfiguration.applyMeta(section(), meta)));
    }

    private ConfigurationSection section() {
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
                    2L,
                    (entity) -> {
                        TextDisplayMetaConfiguration.applyMeta(section(), entity.getMeta());
                        TextDisplayMetaConfiguration.applyTextMeta(section(), entity.getMeta(), player, player);
                        entity.getPassenger().refresh();
                    }
                )
            );
    }

}
