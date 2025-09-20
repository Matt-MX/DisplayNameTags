package com.mattmx.nametags.entity.trait;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class TraitHolder<T> {
    private final @NotNull T owner;
    private final @NotNull Map<Class<?>, Trait<T>> map = new ConcurrentHashMap<>();

    public TraitHolder(@NotNull T owner) {
        this.owner = owner;
    }

    @SuppressWarnings("unchecked")
    public <K extends Trait<T>> @Nullable K getTraitOrNull(@NotNull Class<K> traitClazz) {
        Trait<T> trait = map.get(traitClazz);

        if (trait != null) {
            return (K) trait;
        }
        return null;
    }

    public <K extends Trait<T>> @NotNull Optional<K> getTrait(@NotNull Class<K> traitClazz) {
        return Optional.ofNullable(getTraitOrNull(traitClazz));
    }

    @SuppressWarnings("unchecked")
    public <K extends Trait<T>> @NotNull K getOrAddTrait(@NotNull Class<K> traitClazz, @NotNull Supplier<K> supplier) {
        return (K) map.computeIfAbsent(traitClazz, (k) -> {
            K trait = supplier.get();
            trait.setHolder(owner);

            trait.onEnable();

            return trait;
        });
    }

    @SuppressWarnings("unchecked")
    public <K extends Trait<T>> @Nullable K removeTrait(@NotNull Class<K> traitClazz) {
        Trait<T> trait = map.remove(traitClazz);

        if (trait != null) {
            trait.onDestroy();
            return (K) trait;
        }

        return null;
    }

    public <K extends Trait<T>> boolean hasTrait(@NotNull Class<T> traitClazz) {
        return map.containsKey(traitClazz);
    }

    public void destroy() {
        for (Trait<T> trait : map.values()) {
            trait.onDestroy();
        }
        map.clear();
    }
}
