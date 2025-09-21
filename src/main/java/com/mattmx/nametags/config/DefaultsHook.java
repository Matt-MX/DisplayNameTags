package com.mattmx.nametags.config;

import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.config.groups.ConfigGroup;
import com.mattmx.nametags.entity.NameTagHolder;
import com.mattmx.nametags.event.NameTagCreateEvent;
import com.mattmx.nametags.utils.Dependencies;
import lombok.Getter;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class DefaultsHook implements Listener {
    private final Set<ConfigGroup> groups = ConcurrentHashMap.newKeySet();
    private final ConfigGroup defaultGroup;
    private final DefaultsProvider defaultsProvider;

    public DefaultsHook(@NotNull NameTags plugin) {
        ConfigurationSection defaultsSection = Objects.requireNonNull(plugin.getConfig().getConfigurationSection("defaults"));
        this.defaultGroup = new ConfigGroup("default", defaultsSection);
        this.defaultsProvider = new DefaultsProvider(defaultGroup);

        plugin.getEntityManager().setDefaultProvider(defaultsProvider);

        for (ConfigGroup group : groups) {
            Bukkit.getPluginManager().removePermission(group.getPermissionNode());
        }

        groups.clear();

        ConfigurationSection groups = plugin.getConfig().getConfigurationSection("groups");

        if (groups != null) {
            for (String key : groups.getKeys(false)) {
                ConfigurationSection sub = groups.getConfigurationSection(key);

                if (sub == null) continue;
                ConfigGroup group = new ConfigGroup(key, sub);

                this.groups.add(group);
                try {
                    Bukkit.getPluginManager().addPermission(new Permission(group.getPermissionNode()));
                } catch (Exception ignored) {
                }
            }
        }

        // If LuckPerms is installed, listen for updates in the players'
        // rank to recalculate the necessary groups to apply.
        if (Dependencies.LUCKPERMS.isPresent()) {
            LuckPermsProvider.get().getEventBus().subscribe(UserDataRecalculateEvent.class, (event) -> {
                final UUID uniqueId = event.getUser().getUniqueId();
                final NameTagHolder holder = plugin.getEntityManager().getNameTagHolderByUUID(uniqueId);
                if (holder != null) {
                    holder.getTraits().getTrait(DefaultsTrait.class).ifPresent(DefaultsTrait::updateGroup);
                }
            });
        }
    }

    @EventHandler
    public void onCreateNameTag(@NotNull NameTagCreateEvent event) {
        final NameTagHolder holder = event.getNameTag();

        if (!(holder.getOwner() instanceof Player player)) {
            return;
        }

        holder.getTraits().removeTrait(DefaultsTrait.class);
        holder.getTraits().getOrAddTrait(DefaultsTrait.class, DefaultsTrait::new).updateGroup();
    }

}
