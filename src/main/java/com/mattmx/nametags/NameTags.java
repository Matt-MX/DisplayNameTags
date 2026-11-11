package com.mattmx.nametags;

import com.github.retrooper.packetevents.PacketEvents;
import com.mattmx.nametags.config.Settings;
import com.mattmx.nametags.config.TextFormatter;
import com.mattmx.nametags.defaults.ConfigDefaultsListener;
import com.mattmx.nametags.display.TagDisplay;
import com.mattmx.nametags.entity.NameTagEntityManager;
import com.mattmx.nametags.hook.NeznamyTABHook;
import com.mattmx.nametags.hook.SkinRestorerHook;
import com.mattmx.nametags.packet.OutgoingPacketListener;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NameTags extends JavaPlugin {
    public static final int TRANSPARENT = Color.fromARGB(0).asARGB();
    public static final char LEGACY_CHAR = (char) 167;
    private static final int BSTATS_ID = 25409;

    private static @Nullable NameTags instance;

    private final HashMap<String, ConfigurationSection> groups = new HashMap<>();
    private final NameTagEntityManager entityManager = new NameTagEntityManager();
    private volatile @Nullable Settings settings;
    private @Nullable ConfigDefaultsListener defaultsListener;
    private @Nullable ScoreboardTeams scoreboardTeams;
    private EventsListener eventsListener;
    private OutgoingPacketListener packetListener;
    private Metrics metrics;

    public static @NotNull NameTags getInstance() {
        return Objects.requireNonNull(instance, "NameTags plugin has not initialized yet! Did you forget to depend?");
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        reloadConfig();

        eventsListener = new EventsListener(this);
        packetListener = new OutgoingPacketListener(entityManager);

        metrics = new Metrics(this, BSTATS_ID);
        metrics.addCustomChart(new DrilldownPie("serverName", () -> Map.of(Bukkit.getName(), Map.of(Bukkit.getName(), 1))));

        PacketEvents.getAPI().getEventManager().registerListener(packetListener);
        NeznamyTABHook.inject(this);
        SkinRestorerHook.inject(this);

        Bukkit.getPluginManager().registerEvents(eventsListener, this);
        Bukkit.getScheduler().runTaskLater(this, DependencyVersionChecker::checkPacketEventsVersion, 10L);

        Objects.requireNonNull(Bukkit.getPluginCommand("nametags")).setExecutor(new NameTagsCommand(this));
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        Settings loaded = Settings.load(getConfig());
        settings = loaded;
        TagDisplay.clearFormatted();
        getLogger().info("Using " + loaded.formatter().name() + " as text formatter.");

        if (loaded.disableVanilla() && scoreboardTeams == null) {
            scoreboardTeams = new ScoreboardTeams(this);
            Bukkit.getPluginManager().registerEvents(scoreboardTeams, this);
        } else if (!loaded.disableVanilla() && scoreboardTeams != null) {
            HandlerList.unregisterAll(scoreboardTeams);
            scoreboardTeams = null;
        }

        if (defaultsListener != null) {
            HandlerList.unregisterAll(defaultsListener);
            defaultsListener = null;
        }
        if (loaded.defaultsEnabled()) {
            getLogger().info("Using default behaviour from the config file.");
            defaultsListener = new ConfigDefaultsListener(this);
            Bukkit.getPluginManager().registerEvents(defaultsListener, this);
        }

        registerGroupPermissions();
    }

    private void registerGroupPermissions() {
        for (String permissionNode : groups.keySet()) {
            Bukkit.getPluginManager().removePermission(permissionNode);
        }
        groups.clear();
        ConfigurationSection section = getConfig().getConfigurationSection("groups");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection group = section.getConfigurationSection(key);
            if (group == null) {
                continue;
            }
            String permissionNode = "nametags.groups." + key;
            groups.put(permissionNode, group);
            Bukkit.getPluginManager().addPermission(new Permission(permissionNode));
        }
    }

    @Override
    public void onDisable() {
        if (metrics != null) {
            metrics.shutdown();
        }
        if (eventsListener != null) {
            HandlerList.unregisterAll(eventsListener);
        }
        if (packetListener != null) {
            PacketEvents.getAPI().getEventManager().unregisterListener(packetListener);
        }
    }

    public @NotNull Settings getSettings() {
        return Objects.requireNonNull(settings, "NameTags has not loaded its config yet");
    }

    public @NotNull NameTagEntityManager getEntityManager() {
        return entityManager;
    }

    public @NotNull HashMap<String, ConfigurationSection> getGroups() {
        return groups;
    }

    public @NotNull TextFormatter getFormatter() {
        return getSettings().formatter();
    }
}
