package com.mattmx.nametags;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

public class DependencyVersionChecker {

    /**
     * PacketEvents matches {@link Bukkit#getBukkitVersion()} against the release names it
     * knows, and silently falls back to 1.8.8 when none match. EntityLib derives its entity
     * ID strategy from that same value, so a bad match makes it throw during init.
     * <p>
     * Detecting the fallback directly avoids hard-coding a newest-known version that would
     * go stale every release: if the resolved name is absent from the server version string,
     * PacketEvents guessed.
     * <p>
     * A PacketEvents that is too old tends to fail its own startup outright, after which
     * Paper unloads it and its classes become unreachable -- hence catching {@link Throwable}
     * rather than letting a {@link NoClassDefFoundError} escape.
     */
    public static boolean isPacketEventsUsable() {
        if (!Bukkit.getPluginManager().isPluginEnabled("packetevents")) {
            return false;
        }

        try {
            final ServerVersion resolved = PacketEvents.getAPI()
                .getServerManager()
                .getVersion();

            return Bukkit.getBukkitVersion().contains(resolved.getReleaseName());
        } catch (Throwable error) {
            return false;
        }
    }

    public static void warnUnusablePacketEvents() {
        String detail;
        try {
            final PacketEventsAPI<?> api = PacketEvents.getAPI();
            detail = String.format(
                "PacketEvents %s detected this server as %s, and the newest version it supports is %s.",
                api.getVersion().toStringWithoutSnapshot(),
                api.getServerManager().getVersion().getReleaseName(),
                ServerVersion.getLatest().getReleaseName()
            );
        } catch (Throwable error) {
            detail = "PacketEvents is not loaded, so it most likely failed to start on this Minecraft version.";
        }

        NameTags.getInstance().getComponentLogger().warn(Component.text(String.format("""
            
            ⚠ NameTags cannot start because PacketEvents does not support this server.
            
            Server version: %s
            %s
            
            Please update to a PacketEvents build that supports this Minecraft version.
            Download it here: https://modrinth.com/plugin/packetevents
            
            """, Bukkit.getBukkitVersion(), detail)));
    }
}
