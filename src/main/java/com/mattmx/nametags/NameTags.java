package com.mattmx.nametags;

import com.github.retrooper.packetevents.PacketEvents;
import com.mattmx.nametags.entity.NameTagEntityManager;
import me.tofaa.entitylib.APIConfig;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NameTags extends JavaPlugin {
    private static @Nullable NameTags instance;

    private NameTagEntityManager entityManager;
    private final EventsListener eventsListener = new EventsListener();
    private final OutgoingPacketListener packetListener = new OutgoingPacketListener(this);

    @Override
    public void onEnable() {
        instance = this;
        entityManager = new NameTagEntityManager();

        SpigotEntityLibPlatform platform = new SpigotEntityLibPlatform(this);
        APIConfig settings = new APIConfig(PacketEvents.getAPI())
            .debugMode()
            .tickTickables()
            .trackPlatformEntities()
            .usePlatformLogger();

        EntityLib.init(platform, settings);

        PacketEvents.getAPI()
            .getEventManager()
            .registerListener(packetListener);

        Bukkit.getPluginManager().registerEvents(eventsListener, this);
    }

    public @NotNull NameTagEntityManager getEntityManager() {
        return this.entityManager;
    }

    public static @NotNull NameTags getInstance() {
        if (instance == null)
            throw new RuntimeException("NameTags plugin has not initialized yet! Did you forget to depend?");

        return instance;
    }
}
