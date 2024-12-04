package com.mattmx.nametags.extras;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

public class BukkitUtils {

    public static void ensureNotOnPrimaryThread(@Nullable String methodName) {
        if (Bukkit.isPrimaryThread()) {
            final String message = methodName == null
                ? "This method should not be called from the primary thread."
                : String.format("The method %s should not be called from the primary thread.", methodName);
            throw new IllegalCallerException(message);
        }
    }

}
