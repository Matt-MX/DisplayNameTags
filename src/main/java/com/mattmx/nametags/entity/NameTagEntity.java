package com.mattmx.nametags.entity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.config.Settings;
import com.mattmx.nametags.display.TagDisplay;
import com.mattmx.nametags.display.TagMeta;
import com.mattmx.nametags.display.TagViewers;
import com.mattmx.nametags.entity.trait.TraitHolder;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class NameTagEntity {
    private final @NotNull TraitHolder traits = new TraitHolder(this);
    private final @NotNull Entity bukkitEntity;
    private final @NotNull TagDisplay passenger;

    public NameTagEntity(@NotNull Entity entity, @NotNull TagViewers viewers) {
        this.bukkitEntity = entity;
        this.passenger = TagDisplay.create(entity, viewers);
    }

    public void initialize() {
        passenger.spawn(updateLocation());
        if (bukkitEntity instanceof Player self && SelfVisibility.shown(self)) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(self);
            if (user != null) {
                showTo(user);
            }
        }
    }

    public void showTo(@NotNull User user) {
        updateLocation();
        passenger.addViewer(user);
        user.sendPacket(getPassengersPacket());
    }

    public void showTo(@NotNull User user, double x, double y, double z) {
        passenger.setLocation(new Location(x, y + bukkitEntity.getHeight(), z, 0f, 0f));
        passenger.addViewer(user);
        user.sendPacket(getPassengersPacket());
    }

    public void updateFlags(boolean sneaking, boolean invisible) {
        Settings settings = NameTags.getInstance().getSettings();
        boolean sneak = sneaking && settings.sneakEnabled();
        TagMeta meta = passenger.getMeta();
        if (meta.isSneaking() == sneak && meta.isHidden() == invisible) {
            return;
        }
        meta.setSneaking(sneak, settings.sneakOpacity());
        meta.setHidden(invisible);
        passenger.refreshMeta();
    }

    public void reconcileFlags(boolean sneaking, boolean invisible) {
        Settings settings = NameTags.getInstance().getSettings();
        TagMeta meta = passenger.getMeta();
        meta.setSneaking(sneaking && settings.sneakEnabled(), settings.sneakOpacity());
        meta.setHidden(invisible);
    }

    public boolean isInvisible() {
        return bukkitEntity.isInvisible()
            || bukkitEntity instanceof LivingEntity living && living.hasPotionEffect(PotionEffectType.INVISIBILITY);
    }

    public void updateVisibility() {
        updateVisibility(isInvisible());
    }

    public void updateVisibility(boolean isInvisible) {
        passenger.getMeta().setHidden(isInvisible);
    }

    public @NotNull TraitHolder getTraits() {
        return traits;
    }

    public void modify(boolean applyUpdates, @NotNull Consumer<TagMeta> consumer) {
        consumer.accept(passenger.getMeta());
        if (applyUpdates) {
            passenger.refresh();
        }
    }

    public void modify(@NotNull Consumer<TagMeta> consumer) {
        modify(true, consumer);
    }

    public @NotNull TagMeta getMeta() {
        return passenger.getMeta();
    }

    public void sendPassengerPacket(@NotNull Player target) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(target, getPassengersPacket());
    }

    public @NotNull PacketWrapper<?> getPassengersPacket() {
        int[] passengers = NameTags.getInstance()
            .getEntityManager()
            .getLastSentPassengers(bukkitEntity.getEntityId())
            .orElseGet(this::currentPassengers);
        return new WrapperPlayServerSetPassengers(bukkitEntity.getEntityId(), passengers);
    }

    private int[] currentPassengers() {
        List<Entity> riders = bukkitEntity.getPassengers();
        int[] passengers = new int[riders.size() + 1];
        for (int i = 0; i < riders.size(); i++) {
            passengers[i] = riders.get(i).getEntityId();
        }
        passengers[riders.size()] = passenger.getEntityId();
        return passengers;
    }

    public @NotNull Entity getBukkitEntity() {
        return bukkitEntity;
    }

    public @NotNull TagDisplay getPassenger() {
        return passenger;
    }

    public @NotNull Location updateLocation() {
        org.bukkit.Location bukkitLocation = bukkitEntity.getLocation();
        bukkitLocation.setY(bukkitEntity.getBoundingBox().getMaxY());
        Location location = SpigotConversionUtil.fromBukkitLocation(bukkitLocation);
        location.setYaw(0f);
        location.setPitch(0f);
        passenger.setLocation(location);
        return location;
    }

    public void destroy() {
        passenger.despawn();
        traits.destroy();
    }
}
