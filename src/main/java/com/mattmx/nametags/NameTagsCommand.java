package com.mattmx.nametags;

import com.mattmx.nametags.entity.NameTagEntity;
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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // SHUT UP EVA

        if (args.length == 0) {
            return false;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            reload();
            sender.sendMessage(Component.text("Reloaded!").color(NamedTextColor.GREEN));
        } else if (args[0].equalsIgnoreCase("debug")) {
            sender.sendMessage(
                    Component.text("NameTags debug")
                            .appendNewline()
                            .append(
                                    Component.text("Total NameTags: " + plugin.getEntityManager().getCacheSize())
                                            .hoverEvent(HoverEvent.showText(
                                                    Component.text("By Entity UUID: " + plugin.getEntityManager().getCacheSize())
                                                            .appendNewline()
                                                            .append(Component.text("By Entity ID: " + plugin.getEntityManager().getEntityIdMapSize()))
                                                            .appendNewline()
                                                            .append(Component.text("By Passenger ID: " + plugin.getEntityManager().getPassengerIdMapSize()))
                                            ))
                                            .color(NamedTextColor.WHITE)
                            )
                            .appendNewline()
                            .append(
                                    Component.text("Cached last sent passengers: " + plugin.getEntityManager().getLastSentPassengersSize())
                                            .color(NamedTextColor.WHITE)
                            )
                            .appendNewline()
                            .append(
                                    Component.text("Viewers:")
                                            .appendNewline()
                                            .append(
                                                    Component.text(
                                                            String.join("\n",
                                                                    plugin.getEntityManager()
                                                                            .getAllEntities()
                                                                            .stream()
                                                                            .map((nameTag) -> " - " + nameTag.getBukkitEntity().getUniqueId() + ": " + nameTag.getPassenger().getViewers())
                                                                            .toList()
                                                            )
                                                    )
                                            )
                            )
                            .color(NamedTextColor.GOLD)
            );
        }

        return false;
    }

    private void reload() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final NameTagEntity tag = plugin.getEntityManager().getNameTagEntity(player);

            if (tag != null) {
                tag.getTraits().destroy();
            }
        }

        this.plugin.reloadConfig();

        for (final Player player : Bukkit.getOnlinePlayers()) {
            final NameTagEntity tag = plugin.getEntityManager().removeEntity(player);

            if (tag != null) {
                tag.destroy();
            }

            final NameTagEntity newTag = plugin.getEntityManager().getOrCreateNameTagEntity(player);

            // Add existing viewers
            if (tag != null) {
                for (final UUID viewer : tag.getPassenger().getViewers()) {
                    newTag.getPassenger().addViewer(viewer);

                    // Send passenger packet
                    Player playerViewer = Bukkit.getPlayer(viewer);
                    if (playerViewer != null) {
                        newTag.sendPassengerPacket(playerViewer);
                    }
                }
            }

            newTag.updateVisibility();
        }
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        String lastArg = args.length >= 1 ? args[0].toLowerCase() : "";
        return Stream.of("reload", "debug")
                .filter((arg) -> arg.toLowerCase().startsWith(lastArg))
                .toList();
    }
}
