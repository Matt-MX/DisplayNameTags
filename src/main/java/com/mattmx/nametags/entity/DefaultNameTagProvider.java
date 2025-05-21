package com.mattmx.nametags.entity;

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

public class DefaultNameTagProvider {
    private final @NotNull List<BiConsumer<Entity, TextDisplayMeta>> lineProviders;

    public DefaultNameTagProvider(@NotNull List<BiConsumer<Entity, TextDisplayMeta>> lineProviders) {
        this.lineProviders = lineProviders;
    }

    public void addLine(@NotNull BiConsumer<Entity, TextDisplayMeta> lineProvider) {
        this.lineProviders.add(lineProvider);
    }

    public void apply(@NotNull NameTagHolder holder) {
        for (BiConsumer<Entity, TextDisplayMeta> provider : lineProviders) {
            WrapperEntity newEntity = new WrapperEntity(EntityTypes.TEXT_DISPLAY);
            provider.accept(holder.getBukkitEntity(), newEntity.getEntityMeta(TextDisplayMeta.class));
            holder.getPassengers().add(newEntity);
        }
    }

    public static class Builder {
        private final @NotNull List<BiConsumer<Entity, TextDisplayMeta>> lineProviders = new LinkedList<>();

        public Builder addLine(@NotNull BiConsumer<Entity, TextDisplayMeta> lineProvider) {
            this.lineProviders.add(lineProvider);
            return this;
        }

        public DefaultNameTagProvider build() {
            return new DefaultNameTagProvider(lineProviders);
        }
    }
}
