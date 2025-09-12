package com.mattmx.nametags.event;

import com.mattmx.nametags.entity.NameTagHolder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class NameTagCreateEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final @NotNull NameTagHolder nameTag;

    public NameTagCreateEvent(@NotNull NameTagHolder holder) {
        super(!Bukkit.isPrimaryThread());

        this.nameTag = holder;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return getHandlerList();
    }
}
