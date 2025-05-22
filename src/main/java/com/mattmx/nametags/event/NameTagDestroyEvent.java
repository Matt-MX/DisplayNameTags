package com.mattmx.nametags.event;

import com.mattmx.nametags.entity.NameTagHolder;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NameTagDestroyEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final @NotNull NameTagHolder nameTag;

    public NameTagDestroyEvent(@NotNull NameTagHolder nameTag) {
        super(!Bukkit.isPrimaryThread());

        this.nameTag = nameTag;
    }

    public @NotNull NameTagHolder getNameTag() {
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
