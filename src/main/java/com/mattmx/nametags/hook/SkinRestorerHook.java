package com.mattmx.nametags.hook;

import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.entity.NameTagEntity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.event.SkinApplyEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class SkinRestorerHook {

    public static void inject(@NotNull NameTags plugin) {
        Bukkit.getScheduler().runTask(plugin, SkinRestorerHook::start);
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

        new BukkitRunnable() {
            @Override
            public void run() {
                NameTags plugin = NameTags.getInstance();

                plugin.getEntityManager().removeLastSentPassengersCache(player.getEntityId());

                NameTagEntity entity = plugin.getEntityManager().removeEntity(player);

                if (entity != null) {
                    entity.destroy();
                }

                NameTagEntity newEntity = plugin.getEntityManager().getOrCreateNameTagEntity(player);
                newEntity.updateVisibility();
                newEntity.updateLocation();

                if (plugin.getConfig().getBoolean("show-self", false)) {
                    newEntity.getPassenger().removeViewer(newEntity.getBukkitEntity().getUniqueId());
                    newEntity.getPassenger().addViewer(newEntity.getBukkitEntity().getUniqueId());
                    newEntity.sendPassengerPacket(event.getPlayer(Player.class));

                    player.sendMessage(Component.text("Please re-join for update your nametag!").color(NamedTextColor.GREEN));
                }
            }
        }.runTask(NameTags.getInstance());
    }
}