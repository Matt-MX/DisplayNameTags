package com.mattmx.nametags;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mattmx.nametags.config.ConfigDefaultsListener;
import com.mattmx.nametags.config.TextFormatter;
import com.mattmx.nametags.entity.NameTagEntityManager;
import com.mattmx.nametags.hook.NeznamyTABHook;
import com.mattmx.nametags.hook.SkinRestorerHook;
import me.tofaa.entitylib.APIConfig;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NameTags extends JavaPlugin {
    public static final int TRANSPARENT = Color.fromARGB(0).asARGB();
    public static final char LEGACY_CHAR = (char) 167;
    private static @Nullable NameTags instance;
    private final HashMap<String, ConfigurationSection> groups = new HashMap<>();
    private @Nullable Executor executor = null;
    private @NotNull TextFormatter formatter = TextFormatter.MINI_MESSAGE;
    private NameTagEntityManager entityManager;
    private EventsListener eventsListener;
    private OutgoingPacketListener packetListener;
    private Metrics metrics;
    private @Nullable ConfigDefaultsListener defaultsListener = null;

    public static @NotNull NameTags getInstance() {
        return Objects.requireNonNull(instance, "NameTags plugin has not initialized yet! Did you forget to depend?");
    }

    @Override
    public void onEnable() {
        instance = this;

        entityManager = new NameTagEntityManager();
        eventsListener = new EventsListener(this);
        packetListener = new OutgoingPacketListener(this);

        saveDefaultConfig();

        metrics = new Metrics(this, 25409);
        registerMetrics();

        executor = Executors.newFixedThreadPool(
                getConfig().getInt("options.threads", 2),
                new ThreadFactoryBuilder()
                        .setPriority(Thread.NORM_PRIORITY + 1)
                        .setNameFormat("NameTags-Processor")
                        .build()
        );

        SpigotEntityLibPlatform platform = new SpigotEntityLibPlatform(this);
        APIConfig settings = new APIConfig(PacketEvents.getAPI()).usePlatformLogger();

        EntityLib.init(platform, settings);

        final PacketEventsAPI<?> packetEvents = PacketEvents.getAPI();

        packetEvents.getEventManager().registerListener(packetListener);
//        packetEvents.getEventManager().registerListener(new GlowingEffectHook());

        NeznamyTABHook.inject(this);
        SkinRestorerHook.inject(this);

        Bukkit.getPluginManager().registerEvents(eventsListener, this);
        Bukkit.getScheduler().runTaskLater(this, DependencyVersionChecker::checkPacketEventsVersion, 10L);

        Objects.requireNonNull(Bukkit.getPluginCommand("nametags")).setExecutor(new NameTagsCommand(this));
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        ConfigurationSection defaults = getConfig().getConfigurationSection("defaults");
        if (defaults != null && defaults.getBoolean("enabled")) {
            getLogger().info("Using default behaviour from the config file.");

            if (defaultsListener != null) {
                HandlerList.unregisterAll(defaultsListener);
            }

            defaultsListener = new ConfigDefaultsListener(this);
            Bukkit.getPluginManager().registerEvents(defaultsListener, this);
        }

        String textFormatterIdentifier = getConfig().getString("formatter", "minimessage");
        formatter = TextFormatter.getById(textFormatterIdentifier)
                .orElse(TextFormatter.MINI_MESSAGE);

        getLogger().info("Using " + formatter.name() + " as text formatter.");

        for (String permissionNode : groups.keySet()) {
            Bukkit.getPluginManager().removePermission(permissionNode);
        }
        groups.clear();

        ConfigurationSection groups = getConfig().getConfigurationSection("groups");

        if (groups == null) return;

        for (String key : groups.getKeys(false)) {
            String permissionNode = "nametags.groups." + key;
            ConfigurationSection sub = groups.getConfigurationSection(key);

            if (sub == null) continue;

            this.groups.put(permissionNode, sub);

            Bukkit.getPluginManager().addPermission(new Permission(permissionNode));
        }
    }

    public void registerMetrics() {
        metrics.addCustomChart(new DrilldownPie("serverName", () -> Map.of(Bukkit.getName(), Map.of(Bukkit.getName(), 1))));
    }

    @Override
    public void onDisable() {
        metrics.shutdown();

        HandlerList.unregisterAll(this.eventsListener);

        PacketEvents.getAPI()
                .getEventManager()
                .unregisterListener(this.packetListener);
    }

    public Executor getExecutor() {
        if (this.executor == null) {
            throw new RuntimeException("Executor is not available until the plugin has initialized.");
        }

        return this.executor;
    }

    public @NotNull NameTagEntityManager getEntityManager() {
        return this.entityManager;
    }

    public HashMap<String, ConfigurationSection> getGroups() {
        return groups;
    }

    public @NotNull TextFormatter getFormatter() {
        return this.formatter;
    }
}
