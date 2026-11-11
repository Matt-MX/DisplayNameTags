package com.mattmx.nametags.entity;

import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.config.Settings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;

public final class SelfVisibility {

    private SelfVisibility() {
    }

    public static boolean shown(@NotNull Player player) {
        Settings settings = NameTags.getInstance().getSettings();
        if (!settings.showSelf()) {
            return false;
        }
        return settings.showSelfBedrock() || !isBedrock(player);
    }

    private static boolean isBedrock(@NotNull Player player) {
        if (Bukkit.getPluginManager().isPluginEnabled("floodgate")) {
            return FloodgateApi.getInstance().isFloodgateId(player.getUniqueId());
        }
        return player.getUniqueId().getMostSignificantBits() == 0;
    }
}
