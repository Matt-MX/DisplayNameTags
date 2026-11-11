package com.mattmx.nametags.defaults;

import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.config.Settings;
import com.mattmx.nametags.event.NameTagEntityCreateEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class ConfigDefaultsListener implements Listener {
    private static final int INTERPOLATION_TICKS = 5;

    private final @NotNull NameTags plugin;

    public ConfigDefaultsListener(@NotNull NameTags plugin) {
        this.plugin = plugin;
        plugin.getEntityManager().setDefaultProvider((entity, meta) -> {
            meta.setUseDefaultBackground(false);
            meta.setTransformationInterpolationDuration(INTERPOLATION_TICKS);
            meta.setPositionRotationInterpolationDuration(INTERPOLATION_TICKS);
            plugin.getSettings().defaults().style().applyTo(meta);
        });
    }

    @EventHandler
    public void onCreate(@NotNull NameTagEntityCreateEvent event) {
        if (!(event.getNameTag().getBukkitEntity() instanceof Player player)) {
            return;
        }
        Settings settings = plugin.getSettings();
        long period = settings.defaults().refreshEvery();
        if (period == 0L) {
            return;
        }
        DefaultsRefresh.start(plugin, event.getNameTag(), player, period);
    }
}
