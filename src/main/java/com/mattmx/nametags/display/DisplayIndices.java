package com.mattmx.nametags.display;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

final class DisplayIndices {
    static final int TRANSFORMATION_INTERPOLATION = 9;
    static final int POSITION_ROTATION_INTERPOLATION = 10;
    static final int TRANSLATION = 11;
    static final int SCALE = 12;
    static final int BILLBOARD = 15;
    static final int BRIGHTNESS = 16;
    static final int VIEW_RANGE = 17;
    static final int SHADOW_RADIUS = 18;
    static final int SHADOW_STRENGTH = 19;
    static final int TEXT = 23;
    static final int LINE_WIDTH = 24;
    static final int BACKGROUND = 25;
    static final int TEXT_OPACITY = 26;
    static final int STYLE_FLAGS = 27;

    // 1.20.2 added Display's position and rotation interpolation duration at index 10, before it every later index is one lower
    static final boolean POSITION_ROTATION_INTERPOLATION_PRESENT = PacketEvents.getAPI()
        .getServerManager()
        .getVersion()
        .isNewerThanOrEquals(ServerVersion.V_1_20_2);

    private DisplayIndices() {
    }

    static int onServer(int modern) {
        return POSITION_ROTATION_INTERPOLATION_PRESENT || modern < POSITION_ROTATION_INTERPOLATION ? modern : modern - 1;
    }
}
