package com.mattmx.nametags;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.util.PEVersion;
import net.kyori.adventure.text.Component;

public class DependencyVersionChecker {

    public static void checkPacketEventsVersion() {
        final PacketEventsAPI<?> api = PacketEvents.getAPI();

        PEVersion currentPEVersion = api.getVersion();
        ServerVersion outdatedMCVersion = ServerVersion.V_1_21_4;

        boolean isOutdated = currentPEVersion.isOlderThan(new PEVersion(2, 8, 0));
        boolean isUnsupported = api.getServerManager().getVersion().isNewerThan(outdatedMCVersion);

        if (isOutdated && isUnsupported) {
            NameTags.getInstance().getComponentLogger().warn(Component.text(String.format("""
                
                ⚠ Detected PacketEvents version %s, which does not support Minecraft versions newer than %s!
                
                Please update to the latest PacketEvents release to ensure compatibility.
                Download it here: https://modrinth.com/plugin/packetevents
                
                """, currentPEVersion.toStringWithoutSnapshot(), outdatedMCVersion.getReleaseName())));
        }
    }
}
