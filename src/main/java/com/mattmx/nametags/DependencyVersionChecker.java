package com.mattmx.nametags;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.util.PEVersion;
import net.kyori.adventure.text.Component;

public class DependencyVersionChecker {

    public static void checkPacketEventsVersion() {
        final PacketEventsAPI<?> api = PacketEvents.getAPI();

        boolean isOutdated = api.getVersion().isOlderThan(PEVersion.fromString("2.7.0"));
        boolean isUnsupported = api.getServerManager().getVersion().isNewerThan(ServerVersion.V_1_21_4);

        if (isOutdated && isUnsupported) {
            NameTags.getInstance().getComponentLogger().warn(Component.text("""
                    
                    ⚠ PacketEvents version 2.7.0 does not support versions newer than 1.21.4!
                    
                    Please update to a development 2.8.0 build that adds 1.21.5+ support.
                    https://ci.codemc.io/job/retrooper/job/packetevents/
                    
                    """));
        }
    }

}
