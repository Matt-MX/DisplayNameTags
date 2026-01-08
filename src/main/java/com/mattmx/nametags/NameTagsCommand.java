package com.mattmx.nametags;

import com.mattmx.nametags.entity.NameTagEntity;
import com.mattmx.nametags.utils.BedrockUtil;
import com.mattmx.nametags.utils.SelfVisibilityUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class NameTagsCommand implements CommandExecutor, TabCompleter {
    private final @NotNull NameTags plugin;

    public NameTagsCommand(@NotNull NameTags plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
        @NotNull CommandSender sender,
        @NotNull Command command,
        @NotNull String label,
        @NotNull String[] args
    ) {
        if(
            sender instanceof Player player
            && !player.hasPermission("nametags.command.admin")
        ) {
            player.sendMessage(Component.text(
                "You don't have permission to execute this command.",
                NamedTextColor.RED
            ));
            return true;
        }

        if(args.length == 0) {
            sender.sendMessage(Component.text("Invalid parameters."));
        }

        if(args[0].equalsIgnoreCase("reload")) {
            this.reload();
            sender.sendMessage(Component.text(
                "Reloaded!",
                NamedTextColor.GREEN
            ));

            return true;
        }

        sender.sendMessage(Component.text("Invalid parameters."));
        return true;
    }

        private void reload() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final NameTagEntity tag = plugin.getEntityManager().getNameTagEntity(player);

            if (tag != null) {
                tag.getTraits().destroy();
                tag.destroy();
            }
        }

        this.plugin.reloadConfig();

        final var conf = plugin.getConfig();

        for (final Player player : Bukkit.getOnlinePlayers()) {
            final NameTagEntity newTag = plugin.getEntityManager().getOrCreateNameTagEntity(player);

            // Add existing viewers
            // TODO: this bad, spectator n stuff
            for (final Player viewer : Bukkit.getOnlinePlayers()) {
                if (
                    viewer == player
                    && !SelfVisibilityUtil.shouldShowSelf(viewer)
                ) {
                    continue;
                }

                newTag.getPassenger().addViewer(viewer.getUniqueId());
                newTag.sendPassengerPacket(viewer);
            }

            newTag.updateVisibility();
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(
        @NotNull CommandSender commandSender,
        @NotNull Command command,
        @NotNull String s,
        @NotNull String[] args
    ) {
        String lastArg = args.length >= 1
            ? args[0].toLowerCase()
            : "";

        return Stream.of("reload", "debug")
                .filter((arg) -> arg.toLowerCase().startsWith(lastArg))
                .toList();
    }
}