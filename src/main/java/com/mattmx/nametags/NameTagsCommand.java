package com.mattmx.nametags;

import com.mattmx.nametags.entity.NameTagEntity;
import com.mattmx.nametags.entity.NameTagEntityManager;
import net.kyori.adventure.text.Component;
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
            return true;
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
        NameTagEntityManager tags = plugin.getEntityManager();
        for (Player player : Bukkit.getOnlinePlayers()) {
            NameTagEntity tag = tags.removeEntity(player);
            if (tag != null) {
                tag.destroy();
            }
        }
        plugin.reloadConfig();
        for (Player player : Bukkit.getOnlinePlayers()) {
            NameTagEntity tag = tags.getOrCreateNameTagEntity(player);
            tag.updateVisibility();
            tags.showToTrackers(tag);
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