package com.mattmx.nametags.config.groups;

import com.mattmx.nametags.entity.NameTagEntity;
import lombok.Getter;
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Getter
public class ConfigurableEntry<T, E extends AbstractDisplayMeta> {
    public static final Map<String, ConfigurableEntry<?, ?>> ENTRIES = new HashMap<>();

    static {
        addEntries(
            new ConfigurableEntry<>(
                List.of("translate", "translation", "offset"),
                ConfigValueSupplier.vector3f(),
                AbstractDisplayMeta.class,
                AbstractDisplayMeta::getTranslation,
                AbstractDisplayMeta::setTranslation
            ),
            new ConfigurableEntry<>(
                List.of("scale"),
                ConfigValueSupplier.vector3f(),
                AbstractDisplayMeta.class,
                AbstractDisplayMeta::getScale,
                AbstractDisplayMeta::setScale
            ),
            new ConfigurableEntry<>(
                List.of("gap"),
                ConfigValueSupplier.floatA(),
                AbstractDisplayMeta.class,
                (meta) -> meta.getTranslation().y,
                (meta, value) -> meta.setTranslation(meta.getTranslation().withY(value))
            ),
            new ConfigurableEntry<>(
                List.of("billboard"),
                ConfigValueSupplier.enumSet(AbstractDisplayMeta.BillboardConstraints.class, AbstractDisplayMeta.BillboardConstraints.CENTER),
                AbstractDisplayMeta.class,
                AbstractDisplayMeta::getBillboardConstraints,
                AbstractDisplayMeta::setBillboardConstraints
            ),
            new ConfigurableEntry<>(
                List.of("text-shadow", "textshadow"),
                ConfigValueSupplier.bool(),
                TextDisplayMeta.class,
                TextDisplayMeta::isShadow,
                TextDisplayMeta::setShadow
            ),
            new ConfigurableEntry<>(
                List.of("line-width", "width", "linewidth"),
                ConfigValueSupplier.integer(),
                TextDisplayMeta.class,
                TextDisplayMeta::getBackgroundColor,
                TextDisplayMeta::setBackgroundColor
            ),
            new ConfigurableEntry<>(
                List.of("see-through", "see-thru"),
                ConfigValueSupplier.bool(),
                TextDisplayMeta.class,
                TextDisplayMeta::isSeeThrough,
                TextDisplayMeta::setSeeThrough
            ),
            new ConfigurableEntry<>(
                List.of("brightness"),
                ConfigValueSupplier.integer(),
                AbstractDisplayMeta.class,
                AbstractDisplayMeta::getBrightnessOverride,
                AbstractDisplayMeta::setBrightnessOverride
            )
        );
    }

    public static Map<String, ConfigurableEntry<?, ?>> filterEntries(ConfigurationSection section) {
        Map<String, ConfigurableEntry<?, ?>> map = new HashMap<>();

        for (Map.Entry<String, ConfigurableEntry<?, ?>> mapEntry : ENTRIES.entrySet()) {
            if (section.get(mapEntry.getKey()) != null) {
                map.put(mapEntry.getKey(), mapEntry.getValue());
            }
        }

        return map;
    }

    public static Map<String, ConfigurableEntry<?, ?>> addEntries(ConfigurableEntry<?, ?>... entries) {
        Map<String, ConfigurableEntry<?, ?>> map = new HashMap<>();

        for (ConfigurableEntry<?, ?> entry : entries) {
            for (String alias : entry.aliases) {
                map.put(alias, entry);
            }
        }

        return map;
    }

    private final Set<String> aliases;
    private final ConfigValueSupplier<T> valueSupplier;
    private final Class<E> metaClass;
    private final Function<E, T> getter;
    private final BiConsumer<E, T> setter;

    public ConfigurableEntry(
        List<String> aliases,
        ConfigValueSupplier<T> valueSupplier,
        Class<E> metaClass,
        Function<E, T> getter,
        BiConsumer<E, T> setter
    ) {
        this.aliases = new HashSet<>(aliases);
        this.valueSupplier = valueSupplier;
        this.metaClass = metaClass;
        this.getter = getter;
        this.setter = setter;
    }

    public void updateIfChanged(final NameTagEntity entity, Object newValue) {
        if (!metaClass.isInstance(entity.getWrapperEntity().getEntityMeta())) {
            return;
        }

        T existingValue = getter.apply(entity.getWrapperEntity().getEntityMeta(metaClass));

        // Ensure values are the same type.
        if (!existingValue.getClass().isInstance(newValue)) {
            return;
        }

        // If values are equal then do not update.
        if (existingValue == newValue) {
            return;
        }

        entity.updateMeta(metaClass, (meta) -> {
            //noinspection unchecked
            setter.accept(meta, (T) newValue);
        });
    }
}
