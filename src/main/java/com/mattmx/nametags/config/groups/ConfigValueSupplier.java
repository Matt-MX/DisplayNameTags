package com.mattmx.nametags.config.groups;

import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@FunctionalInterface
public interface ConfigValueSupplier<T> {

    @Nullable T getValue(String key, ConfigurationSection section);

    static ConfigValueSupplier<String> string() {
        return (key, section) -> section.getString(key);
    }

    static ConfigValueSupplier<Boolean> bool() {
        return (key, section) -> section.getBoolean(key);
    }

    static ConfigValueSupplier<Integer> integer() {
        return (key, section) -> section.getInt(key);
    }

    static <E extends Enum<E>> ConfigValueSupplier<E> enumSet(Class<E> enumClass, E defaultValue) {
        return (key, section) -> {
            String name = section.getString(key);

            if (name == null) {
                return defaultValue;
            }

            for (E e : enumClass.getEnumConstants()) {
                if (e.name().equalsIgnoreCase(name)) {
                    return e;
                }
            }

            return defaultValue;
        };
    }

    static ConfigValueSupplier<List<String>> listOrSingleString() {
        return (key, section) -> {
            Object value = section.get(key);
            if (value instanceof String str) {
                return List.of(str);
            } else if (value instanceof List<?>) {
                return (List<String>) value;
            } else {
                return null;
            }
        };
    }

    static ConfigValueSupplier<Long> longA() {
        return (key, section) -> section.getLong(key);
    }

    static ConfigValueSupplier<Double> doubleA() {
        return (key, section) -> section.getDouble(key);
    }

    static ConfigValueSupplier<Float> floatA() {
        return (key, section) -> (float) section.getDouble(key);
    }

    static ConfigValueSupplier<Vector3d> vector3d() {
        return (key, section) -> {
            ConfigurationSection sub = section.getConfigurationSection(key);

            if (sub != null) {
                return new Vector3d(sub.getDouble("x"), sub.getDouble("y"), sub.getDouble("z"));
            }

            return null;
        };
    }

    static ConfigValueSupplier<Vector3f> vector3f() {
        return (key, section) -> {
            ConfigurationSection sub = section.getConfigurationSection(key);

            if (sub != null) {
                return new Vector3f((float) sub.getDouble("x"), (float) sub.getDouble("y"), (float) sub.getDouble("z"));
            }

            return null;
        };
    }
}
