package com.mattmx.nametags.entity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.entity.trait.TraitHolder;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import lombok.Setter;
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Getter
public class NameTagEntity {
    private final @NotNull TraitHolder<NameTagEntity> traits = new TraitHolder<>(this);
    private final @NotNull WrapperEntity wrapperEntity;
    private final @NotNull NameTagHolder holder;
    private float cachedViewRange = -1f;
    @Setter
    private boolean bypassAutoRemoval = false;

    public NameTagEntity(@NotNull NameTagHolder holder) {
        this.holder = holder;
        this.wrapperEntity = new WrapperEntity(EntityTypes.TEXT_DISPLAY);

        initialize();
    }

    public void notifyChanges(boolean state) {
        wrapperEntity.getEntityMeta().setNotifyAboutChanges(state);
    }

    public void updateTextMeta(@NotNull Consumer<TextDisplayMeta> consumer) {
        this.updateMeta(TextDisplayMeta.class, consumer);
    }

    public <T extends AbstractDisplayMeta> void updateMeta(@NotNull Class<T> clazz, @NotNull Consumer<T> consumer) {
        // Introduce lock?

        notifyChanges(false);
        wrapperEntity.consumeEntityMeta(clazz, consumer);
        notifyChanges(true);
    }

    public void initialize() {
        Location location = updateLocation();

        this.wrapperEntity.spawn(location);

        if (NameTags.getInstance().getConfig().getBoolean("extra.show-self", false)) {

            if (holder.getOwner() instanceof Player self) {
                this.wrapperEntity.addViewer(self.getUniqueId());
                sendPassengerPacket(self);
            }

        }
    }

    public boolean isInvisible() {
        boolean hasInvisibilityEffect = holder.getOwner() instanceof LivingEntity e
            && e.hasPotionEffect(PotionEffectType.INVISIBILITY);

        return holder.getOwner().isInvisible() || hasInvisibilityEffect;
    }

    public void setVisible(boolean visible) {
        notifyChanges(false);

        TextDisplayMeta meta = getTextMeta();

        // If not changed then do not continue
        if (visible == meta.isInvisible()) {
            return;
        }

        if (visible) {
            this.cachedViewRange = meta.getViewRange();
            meta.setViewRange(0f);
        } else {
            meta.setViewRange(this.cachedViewRange);
        }

        notifyChanges(true);
    }

    public @NotNull TraitHolder<NameTagEntity> getTraits() {
        return traits;
    }

    public void modify(Consumer<TextDisplayMeta> consumer) {
        notifyChanges(false);
        this.wrapperEntity.consumeEntityMeta(TextDisplayMeta.class, consumer);
        notifyChanges(true);
    }

    public @NotNull TextDisplayMeta getTextMeta() {
        return this.wrapperEntity.getEntityMeta(TextDisplayMeta.class);
    }

    public void sendPassengerPacket(Player target) {
        PacketEvents.getAPI()
            .getPlayerManager()
            .sendPacket(target, holder.getPassengersPacket());
    }

    public @NotNull Entity getOwner() {
        return holder.getOwner();
    }

    public @NotNull WrapperEntity getWrapperEntity() {
        return wrapperEntity;
    }

    public @NotNull Location updateLocation() {
        Location location = SpigotConversionUtil.fromBukkitLocation(
            getOwner().getLocation().clone().add(0.0, getOwner().getBoundingBox().getMaxY(), 0.0)
        );

        location.setYaw(0f);
        location.setPitch(0f);

        this.wrapperEntity.setLocation(location);

        return location;
    }

    public void destroy() {
        this.getTraits().destroy();
        this.wrapperEntity.despawn();
    }
}
