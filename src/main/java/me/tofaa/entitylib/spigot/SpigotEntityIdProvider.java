package me.tofaa.entitylib.spigot;

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import me.tofaa.entitylib.EntityIdProvider;
import me.tofaa.entitylib.Platform;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Overrides EntityLib's own SpigotEntityIdProvider (the shadowJar keeps this copy, see
 * duplicatesStrategy in build.gradle.kts). Upstream's version calls Paper's
 * UnsafeValues#nextEntityId(), which no longer exists on Paper 26.x, so it throws
 * NoSuchMethodError in its constructor and the whole plugin fails to enable. Tag entities are
 * client-side only, so they just need IDs that never collide with real ones: start far above
 * anything the server will hand out. Same behaviour the production jar ran on before.
 */
public final class SpigotEntityIdProvider implements EntityIdProvider {
    private static final AtomicInteger COUNTER = new AtomicInteger(1_073_741_823);

    public SpigotEntityIdProvider(@NotNull Platform<JavaPlugin> platform) {
    }

    @Override
    public int provide(@NotNull UUID entityUUID, @NotNull EntityType entityType) {
        return COUNTER.incrementAndGet();
    }
}
