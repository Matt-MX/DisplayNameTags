package com.mattmx.nametags.utils.test;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public class TestPlaceholderExpansion extends PlaceholderExpansion implements Relational {
    @Override
    public @NotNull String getIdentifier() {
        return "dev";
    }

    @Override
    public @NotNull String getAuthor() {
        return "MattMX";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    private static final DecimalFormat decimalFormat = new DecimalFormat("#,###.0");

    @Override
    public String onPlaceholderRequest(Player one, Player two, String identifier) {
        if (one == two) {
            return "0.0";
        }

        double blocks = one.getLocation().distance(two.getLocation());
//        double blocks = one.getLocation().distance(one.getLocation().getWorld().getSpawnLocation());

        if (blocks > 1000) {
            double km = blocks / 1000;
            return decimalFormat.format(km) + "k";
        } else {
            return decimalFormat.format(blocks);
        }
    }
}
