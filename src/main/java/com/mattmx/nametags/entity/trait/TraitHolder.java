package com.mattmx.nametags.entity.trait;

import com.mattmx.nametags.entity.NameTagHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Supplier;

public class TraitHolder {
    private final @NotNull NameTagHolder owner;
    private final @NotNull HashMap<Class<?>, Trait> map = new HashMap<>();

    public TraitHolder(@NotNull NameTagHolder owner) {
        this.owner = owner;
    }

    @SuppressWarnings("unchecked")
    public <T extends Trait> @Nullable T getTraitOrNull(@NotNull Class<T> traitClazz) {
        Trait trait = map.get(traitClazz);

        if (trait != null) {
            return (T) trait;
        }
        return null;
    }

    public <T extends Trait> @NotNull Optional<T> getTrait(@NotNull Class<T> traitClazz) {
        return Optional.ofNullable(getTraitOrNull(traitClazz));
    }

    @SuppressWarnings("unchecked")
    public <T extends Trait> @NotNull T getOrAddTrait(@NotNull Class<T> traitClazz, @NotNull Supplier<T> supplier) {
        return (T) map.computeIfAbsent(traitClazz, (k) -> {
            T trait = supplier.get();
            trait.setNameTag(owner);

            trait.onEnable();

            return trait;
        });
    }

    @SuppressWarnings("unchecked")
    public <T extends Trait> @Nullable T removeTrait(@NotNull Class<T> traitClazz) {
        Trait trait = map.remove(traitClazz);

        if (trait != null) {
            trait.onDestroy();
            return (T) trait;
        }

        return null;
    }

    public <T extends Trait> boolean hasTrait(@NotNull Class<T> traitClazz) {
        return map.containsKey(traitClazz);
    }

    public void destroy() {
        for (Trait trait : map.values()) {
            trait.onDestroy();
        }
        map.clear();
    }
}
