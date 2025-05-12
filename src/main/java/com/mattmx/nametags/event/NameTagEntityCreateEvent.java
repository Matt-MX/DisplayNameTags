package com.mattmx.nametags.event;

import com.mattmx.nametags.entity.NameTagEntity;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NameTagEntityCreateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final @NotNull NameTagEntity nameTag;

    public NameTagEntityCreateEvent(@NotNull NameTagEntity nameTag) {
        super(!Bukkit.isPrimaryThread());

        this.nameTag = nameTag;
    }

    public @NotNull NameTagEntity getNameTag() {
        return nameTag;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return getHandlerList();
    }
}
