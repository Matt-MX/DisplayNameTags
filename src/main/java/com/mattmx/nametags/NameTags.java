package com.mattmx.nametags;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.mattmx.nametags.config.ConfigDefaultsListener;
import com.mattmx.nametags.config.TextFormatter;
import com.mattmx.nametags.entity.NameTagEntityManager;
import com.mattmx.nametags.hook.NeznamyTABHook;
import me.tofaa.entitylib.APIConfig;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;

public class NameTags extends JavaPlugin {
    public static final int TRANSPARENT = Color.fromARGB(0).asARGB();
    public static final char LEGACY_CHAR = (char)167;
    private static @Nullable NameTags instance;

    private final HashMap<String, ConfigurationSection> groups = new HashMap<>();
    private @NotNull TextFormatter formatter = TextFormatter.MINI_MESSAGE;
    private NameTagEntityManager entityManager;
    private final EventsListener eventsListener = new EventsListener(this);
    private final OutgoingPacketListener packetListener = new OutgoingPacketListener(this);

    @Override
    public void onEnable() {
        instance = this;
        entityManager = new NameTagEntityManager();
        saveDefaultConfig();

        ConfigurationSection defaults = getConfig().getConfigurationSection("defaults");
        if (defaults != null && defaults.getBoolean("enabled")) {
            Bukkit.getPluginManager().registerEvents(new ConfigDefaultsListener(this), this);
        }

        SpigotEntityLibPlatform platform = new SpigotEntityLibPlatform(this);
        APIConfig settings = new APIConfig(PacketEvents.getAPI())
//            .tickTickables()
//            .trackPlatformEntities()
            .usePlatformLogger();

        EntityLib.init(platform, settings);

        final PacketEventsAPI<?> packetEvents = PacketEvents.getAPI();

        packetEvents.getEventManager().registerListener(packetListener);
//        packetEvents.getEventManager().registerListener(new GlowingEffectHook());

        NeznamyTABHook.inject(this);

        Bukkit.getPluginManager().registerEvents(eventsListener, this);

        Objects.requireNonNull(Bukkit.getPluginCommand("nametags-reload")).setExecutor(new NameTagsCommand(this));
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

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

    public @NotNull NameTagEntityManager getEntityManager() {
        return this.entityManager;
    }

    public HashMap<String, ConfigurationSection> getGroups() {
        return groups;
    }

    public @NotNull TextFormatter getFormatter() {
        return this.formatter;
    }

    public static @NotNull NameTags getInstance() {
        return Objects.requireNonNull(instance, "NameTags plugin has not initialized yet! Did you forget to depend?");
    }
}
