package com.mattmx.nametags;

import com.mattmx.nametags.entity.NameTagEntity;
import com.mattmx.nametags.entity.NameTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class NameTagsCommand implements CommandExecutor {
    private final @NotNull NameTags plugin;

    public NameTagsCommand(@NotNull NameTags plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final NameTagHolder holder = plugin.getEntityManager().getNameTagHolder(player);

            if (holder != null) {
                holder.getTraits().destroy();
            }
        }

        this.plugin.reload();

        for (final Player player : Bukkit.getOnlinePlayers()) {
            final NameTagHolder old = plugin.getEntityManager().removeEntity(player);

            Set<UUID> viewers = Collections.emptySet();
            if (old != null) {
                NameTagEntity first = old.getEntities().getFirst();

                if (first != null) {
                    viewers = first.getWrapperEntity().getViewers();
                }

                old.destroy();
            }

            final NameTagHolder holder = plugin.getEntityManager().getOrCreateNameTagHolder(player);

            // Add existing viewers
            if (old != null) {
                for (final UUID viewer : viewers) {
                    holder.addViewer(viewer);

                    // Send passenger packet
                    Player playerViewer = Bukkit.getPlayer(viewer);
                    if (playerViewer != null) {
                        holder.sendPassengerPacket(playerViewer);
                    }
                }
            }

            holder.manuallyUpdateVisibility();
        }

        sender.sendMessage(Component.text("Reloaded!").color(NamedTextColor.GREEN));
        return false;
    }
}
