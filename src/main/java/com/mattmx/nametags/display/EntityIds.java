package com.mattmx.nametags.display;

import org.bukkit.Bukkit;
import org.bukkit.UnsafeValues;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

final class EntityIds {
    private static final MethodHandle PER_WORLD = find(MethodType.methodType(int.class, World.class));
    private static final MethodHandle GLOBAL = find(MethodType.methodType(int.class));

    private EntityIds() {
    }

    // Paper 26.2 replaced UnsafeValues.nextEntityId() with nextEntityId(World), following vanilla's ServerLevel.getNextEntityId
    static int next(@NotNull World world) {
        try {
            if (PER_WORLD != null) {
                return (int) PER_WORLD.invoke(Bukkit.getUnsafe(), world);
            }
            if (GLOBAL != null) {
                return (int) GLOBAL.invoke(Bukkit.getUnsafe());
            }
        } catch (Throwable error) {
            throw new IllegalStateException("Could not allocate an entity id", error);
        }
        throw new IllegalStateException("This server has no UnsafeValues.nextEntityId");
    }

    private static MethodHandle find(@NotNull MethodType type) {
        try {
            return MethodHandles.publicLookup().findVirtual(UnsafeValues.class, "nextEntityId", type);
        } catch (NoSuchMethodException | IllegalAccessException error) {
            return null;
        }
    }
}
