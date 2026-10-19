package com.mattmx.nametags.hook;

import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.entity.NameTagEntity;
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

    // SkinApplyEvent fires BEFORE SkinsRestorer applies the skin and re-sends the player to everyone
    // around. The old code destroyed the tag and created a brand-new one a tick later: viewers had just
    // been re-attached to the OLD tag by the respawn, then got its destroy packet, and the NEW tag was
    // never sent to anyone already in range — so every player with a SkinsRestorer skin ended up with
    // nothing above their head until they walked out of and back into someone's view. The tag itself
    // doesn't depend on the skin, so keep the same entity and just re-attach it once the refresh is done.
    private static final long REATTACH_DELAY_TICKS = 10L;

    private static void onSkinApply(SkinApplyEvent event) {
        Player player = event.getPlayer(Player.class);

        if (player == null) return;

        Bukkit.getScheduler().runTaskLater(NameTags.getInstance(), () -> {
            if (!player.isOnline()) return;

            NameTags plugin = NameTags.getInstance();
            plugin.getEntityManager().removeLastSentPassengersCache(player.getEntityId());

            NameTagEntity entity = plugin.getEntityManager().getOrCreateNameTagEntity(player);
            entity.updateVisibility();
            entity.resendToTrackers();
        }, REATTACH_DELAY_TICKS);
    }
}