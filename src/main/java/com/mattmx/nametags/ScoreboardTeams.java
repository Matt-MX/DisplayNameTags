package com.mattmx.nametags;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Will send scoreboard teams packets to clients to ensure
 * vanilla name tags are hidden by adding them all to the same team.
 */
public class ScoreboardTeams implements Listener {
    public static final WrapperPlayServerTeams.ScoreBoardTeamInfo TEAM_INFO = new WrapperPlayServerTeams.ScoreBoardTeamInfo(
            Component.empty(),
            Component.empty(),
            Component.empty(),
            WrapperPlayServerTeams.NameTagVisibility.NEVER,
            WrapperPlayServerTeams.CollisionRule.ALWAYS,
            NamedTextColor.WHITE,
            WrapperPlayServerTeams.OptionData.NONE
    );
    public static final String TEAM_NAME = "NameTagsHider";

    public @NotNull NameTags plugin;

    public ScoreboardTeams(@NotNull NameTags plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Bukkit.getAsyncScheduler().runDelayed(this.plugin, (task) -> {
            if (!event.getPlayer().isOnline()) {
                return;
            }

            List<String> usernames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                usernames.add(player.getName());
            }

            usernames.add(event.getPlayer().getName());

            final WrapperPlayServerTeams teamsPacketCreate = new WrapperPlayServerTeams(
                    TEAM_NAME,
                    WrapperPlayServerTeams.TeamMode.CREATE,
                    TEAM_INFO,
                    usernames
            );

            PacketEvents.getAPI()
                    .getPlayerManager()
                    .sendPacketSilently(event.getPlayer(), teamsPacketCreate);

            // Notify existing players of the new player
            final WrapperPlayServerTeams teamsPacketUpdate = new WrapperPlayServerTeams(
                    TEAM_NAME,
                    WrapperPlayServerTeams.TeamMode.ADD_ENTITIES,
                    TEAM_INFO,
                    List.of(event.getPlayer().getName())
            );

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player == event.getPlayer()) continue;

                PacketEvents.getAPI()
                        .getPlayerManager()
                        .sendPacketSilently(player, teamsPacketUpdate);
            }
        }, 500L, TimeUnit.MILLISECONDS);
    }

}
