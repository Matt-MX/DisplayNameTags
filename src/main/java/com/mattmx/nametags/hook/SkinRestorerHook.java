package com.mattmx.nametags.hook;

import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.entity.NameTagEntity;
import com.mattmx.nametags.entity.NameTagEntityManager;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.event.SkinApplyEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SkinRestorerHook {

    public static void inject(@NotNull NameTags plugin) {
        Bukkit.getScheduler().runTaskLater(plugin, SkinRestorerHook::start, 5L);
    }

    private static void start() {
        final boolean isSkinRestorer = Bukkit.getPluginManager().isPluginEnabled("SkinsRestorer");

        if (!isSkinRestorer) return;

        NameTags plugin = NameTags.getInstance();
        SkinsRestorer skinsRestorer = SkinsRestorerProvider.get();

        if (skinsRestorer != null) {
            plugin.getLogger().info("Registering SkinRestorer event listeners.");
            skinsRestorer.getEventBus().subscribe(plugin, SkinApplyEvent.class, SkinRestorerHook::onSkinApply);
        } else {
            plugin.getLogger().warning("SkinsRestorer is enabled, but the API provider is null.");
        }
    }

    private static void onSkinApply(SkinApplyEvent event) {
        Player player = event.getPlayer(Player.class);

        if (player == null) return;

        Bukkit.getScheduler().runTask(NameTags.getInstance(), () -> {
            NameTags plugin = NameTags.getInstance();
            NameTagEntityManager tags = plugin.getEntityManager();
            NameTagEntity previous = tags.removeEntity(player);
            if (previous != null) {
                previous.destroy();
            }
            NameTagEntity tag = tags.getOrCreateNameTagEntity(player);
            tag.updateVisibility();
            tags.showToTrackers(tag);
        });
    }
}
