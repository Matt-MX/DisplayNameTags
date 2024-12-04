package com.mattmx.nametags.extras;

import com.mattmx.nametags.extras.storage.MySQLStorageAdapter;
import com.mattmx.nametags.extras.storage.NameTagStorageAdapter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

public class NameTagsExtras extends JavaPlugin {
    private static @Nullable NameTagsExtras instance;
    private static final HashMap<String, NameTagStorageAdapter> storageAdapters = new HashMap<>();

    private @Nullable NameTagStorageAdapter storage;
    private @NotNull NameTagCache cache = new NameTagCache(this);

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        final String selectedStorageType = getConfig().getString("storage.method", "yaml");
        this.storage = getStorageAdapter(selectedStorageType)
            .orElseThrow(() -> new RuntimeException(String.format("Unknown storage adapter '%s'", selectedStorageType)));

        getLogger().log(Level.INFO, "Using '{}' as storage method.", selectedStorageType);

        getStorage().ifPresent(NameTagStorageAdapter::start);
    }

    @Override
    public void onDisable() {
        getStorage().ifPresent(NameTagStorageAdapter::stop);
    }

    public void reload() {
        getStorage().ifPresent(NameTagStorageAdapter::stop);
        reloadConfig();
        getStorage().ifPresent(NameTagStorageAdapter::start);
    }

    public void registerDefaultAdapters() {
        registerStorageAdapter(MySQLStorageAdapter.IDENTIFIER, new MySQLStorageAdapter(this));
    }

    public @NotNull Optional<NameTagStorageAdapter> getStorage() {
        return Optional.ofNullable(this.storage);
    }

    public @NotNull NameTagCache getCache() {
        return this.cache;
    }

    public static @NotNull NameTagsExtras getInstance() {
        return Objects.requireNonNull(instance, "NameTagsExtras is not initialized! Make sure to add this plugin as a dependency to your plugin!");
    }

    public static <T extends NameTagStorageAdapter> @NotNull Optional<T> getStorageAdapterOf(@NotNull String type) {
        return getStorageAdapter(type).map((adapter) -> (T) adapter);
    }

    public static @NotNull Optional<NameTagStorageAdapter> getStorageAdapter(@NotNull String type) {
        return Optional.ofNullable(storageAdapters.get(type));
    }

    public static void registerStorageAdapter(@NotNull String type, @NotNull NameTagStorageAdapter instance) {
        storageAdapters.put(type, instance);
    }
}