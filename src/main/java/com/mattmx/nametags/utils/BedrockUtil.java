package com.mattmx.nametags.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.UUID;

public class BedrockUtil {
    private static final String PLUGIN_NAME = "floodgate";

    public static boolean isBedrock(final Player player) {
        return isBedrock(player.getUniqueId());
    }

    public static boolean isBedrock(final UUID playerId) {
        if(Bukkit.getPluginManager().isPluginEnabled(PLUGIN_NAME)) {
            return FloodgateApi.getInstance().isFloodgateId(playerId);
        }

        return playerId.getMostSignificantBits() == 0;
    }
}
