package com.mattmx.nametags.extras.command;

import com.mattmx.nametags.extras.NameTagsExtras;
import com.mattmx.nametags.extras.storage.NameTagStorageAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NameTagCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("That is a player only command!").color(NamedTextColor.RED));
            return false;
        }

        final NameTagStorageAdapter storage = NameTagsExtras.getInstance()
            .getStorage()
            .orElseThrow();
        CompletableFuture.supplyAsync(() -> storage.getPlayerNameTag(player.getUniqueId()))
            .thenAccept((result) ->
                result.ifPresentOrElse((nameTag) -> {
                    player.sendMessage(nameTag.text());
                }, () -> player.sendMessage("You have no name tag!")))
            .exceptionally((ex) -> {
                player.sendMessage("Unable to retrieve your name tag");
                return null;
            });

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }

}
