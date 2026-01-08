package com.mattmx.nametags.utils;

import com.mattmx.nametags.NameTags;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelfVisibilityUtil {
    private static final Logger log = LoggerFactory.getLogger(SelfVisibilityUtil.class);

    public static boolean shouldShowSelf(final Player player) {
        final var conf = NameTags.getInstance().getConfig();

        if(!conf.getBoolean("show-self", false)) {
            return false;
        }

        if(BedrockUtil.isBedrock(player) && !conf.getBoolean("show-self-bedrock", false)) {
            return false;
        }

        return true;
    }
}
