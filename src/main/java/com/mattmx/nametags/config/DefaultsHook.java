package com.mattmx.nametags.config;

import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.entity.NameTagHolder;
import com.mattmx.nametags.event.NameTagCreateEvent;
import com.mattmx.nametags.utils.Dependencies;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DefaultsHook implements Listener {
    private final @NotNull NameTags plugin;
    private final DefaultsProvider defaultsProvider = new DefaultsProvider(this);

    public DefaultsHook(@NotNull NameTags plugin) {
        this.plugin = plugin;

        plugin.getEntityManager().setDefaultProvider(defaultsProvider);

        // If LuckPerms is installed, listen for updates in the players'
        // rank to recalculate the necessary groups to apply.
        if (Dependencies.LUCKPERMS.isPresent()) {
            LuckPermsProvider.get().getEventBus().subscribe(UserDataRecalculateEvent.class, (event) -> {
                final UUID uniqueId = event.getUser().getUniqueId();
                final NameTagHolder holder = plugin.getEntityManager().getNameTagHolderByUUID(uniqueId);
                if (holder != null) {
                    holder.getTraits().getTrait(DefaultsTrait.class).ifPresent(DefaultsTrait::updatePermissions);
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
        holder.getTraits().getOrAddTrait(DefaultsTrait.class, DefaultsTrait::new);
    }

}
