package com.mattmx.nametags;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mattmx.nametags.config.DefaultsHook;
import com.mattmx.nametags.config.TextFormatter;
import com.mattmx.nametags.config.groups.ConfigGroup;
import com.mattmx.nametags.entity.NameTagEntityManager;
import com.mattmx.nametags.hook.NeznamyTABHook;
import com.mattmx.nametags.hook.SkinRestorerHook;
import com.mattmx.nametags.utils.Metrics;
import lombok.Getter;
import me.tofaa.entitylib.APIConfig;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NameTags extends JavaPlugin {
    public static final int TRANSPARENT = Color.fromARGB(0).asARGB();
    public static final char LEGACY_CHAR = (char) 167;
    private static @Nullable NameTags instance;
    private @Nullable Executor executor = null;
    @Getter
    private @NotNull TextFormatter formatter = TextFormatter.MINI_MESSAGE;
    @Getter
    private NameTagEntityManager entityManager;
    private EventsListener eventsListener;
    private OutgoingPacketListener packetListener;
    private Metrics metrics;

    @Getter
    private @Nullable DefaultsHook defaults = null;

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
                .setNameFormat("NameTags-Processor-%d")
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

        Objects.requireNonNull(Bukkit.getPluginCommand("nametags-reload")).setExecutor(new NameTagsCommand(this));
    }

    public void reload() {
        HandlerList.unregisterAll(this);
        reloadConfig();

        Bukkit.getPluginManager().registerEvents(eventsListener, this);
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        if (getConfig().getBoolean("disable-vanilla", true)) {
            Bukkit.getPluginManager().registerEvents(new ScoreboardTeams(this), this);
        }

        ConfigurationSection defaults = getConfig().getConfigurationSection("defaults");
        if (defaults != null && defaults.getBoolean("enabled")) {
            getLogger().info("Using default behaviour from the config file.");

            if (this.defaults != null) {
                HandlerList.unregisterAll(this.defaults);
            }

            this.defaults = new DefaultsHook(this);
            Bukkit.getPluginManager().registerEvents(this.defaults, this);
        }

        String textFormatterIdentifier = getConfig().getString("text-formatter", "smart");
        formatter = TextFormatter.getById(textFormatterIdentifier).orElse(TextFormatter.SMART);

        getLogger().info("Using " + formatter.name() + " as text formatter.");
    }

    public void registerMetrics() {
        metrics.addCustomChart(new Metrics.DrilldownPie("serverName", () -> Map.of(Bukkit.getName(), Map.of(Bukkit.getName(), 1))));
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

    public static @NotNull NameTags getInstance() {
        return Objects.requireNonNull(instance, "NameTags plugin has not initialized yet! Did you forget to depend?");
    }
}
