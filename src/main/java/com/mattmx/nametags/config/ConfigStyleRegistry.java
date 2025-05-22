package com.mattmx.nametags.config;

import com.github.retrooper.packetevents.util.Vector3f;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigStyleRegistry {
    private static Map<String, ConfigStyleEntry<?>> registry = new HashMap<>();

    static {
        register(new ConfigStyleEntry<>(
                "scale",
                (obj, holder) -> (Vector3f) obj,
                (vec, entity) -> entity.getEntityMeta(TextDisplayMeta.class).setScale(vec)
        ));

        register(new ConfigStyleEntry<>(
                "translate",
                (obj, holder) -> (Vector3f) obj,
                (vec, entity) -> entity.getEntityMeta(TextDisplayMeta.class).setTranslation(vec)
        ));

        register(new ConfigStyleEntry<>(
                "text",
                (obj, holder) -> obj instanceof String ? List.of((String) obj) : (List<String>) obj,
                (vec, entity) -> {
                    entity.getEntityMeta(TextDisplayMeta.class).setText(vec);
                }
        ));
    }

    public static void register(@NotNull ConfigStyleEntry<?> styleEntry) {
        registry.put(styleEntry.key, styleEntry);
    }

}
