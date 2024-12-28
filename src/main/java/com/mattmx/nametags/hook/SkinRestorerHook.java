package com.mattmx.nametags.hook;

import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.entity.NameTagEntity;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.event.SkinApplyEvent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SkinRestorerHook {

    public SkinRestorerHook() {
        SkinsRestorer skinsRestorer = SkinsRestorerProvider.get();

        if (skinsRestorer != null) {
            skinsRestorer.getEventBus().subscribe(NameTags.getInstance(), SkinApplyEvent.class, this::onSkinApply);
        }
    }

    public void onSkinApply(SkinApplyEvent event) {
        Player player = event.getPlayer(Player.class);

        new BukkitRunnable() {
            @Override
            public void run() {
                NameTags.getInstance().getEntityManager().removeLastSentPassengersCache(player.getEntityId());

                for (final NameTagEntity entity : NameTags.getInstance().getEntityManager().getAllEntities()) {
                    entity.getPassenger().removeViewer(player.getUniqueId());
                }

                NameTagEntity entity = NameTags.getInstance().getEntityManager()
                        .removeEntity(player);

                if (entity != null) entity.destroy();

                NameTagEntity newEntity = NameTags.getInstance().getEntityManager()
                        .getOrCreateNameTagEntity(player);
                newEntity.updateVisibility();
                newEntity.updateLocation();

                newEntity.sendPassengerPacket(player);
            }
        }.runTask(NameTags.getInstance());
    }
}