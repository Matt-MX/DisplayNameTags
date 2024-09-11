package com.mattmx.nametags.entity.trait;

import com.mattmx.nametags.entity.NameTagEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.Supplier;

public class TraitHolder {
    private final @NotNull NameTagEntity owner;
    private final @NotNull HashMap<Class<?>, Trait> map = new HashMap<>();

    public TraitHolder(@NotNull NameTagEntity owner) {
        this.owner = owner;
    }

    @SuppressWarnings("unchecked")
    public <T extends Trait> @Nullable T getTrait(@NotNull Class<T> traitClazz) {
        Trait trait = map.get(traitClazz);

        if (trait != null) {
            return (T) trait;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Trait> @NotNull T getOrAddTrait(@NotNull Class<T> traitClazz, @NotNull Supplier<T> supplier) {
        return (T) map.computeIfAbsent(traitClazz, (k) -> {
            T trait = supplier.get();
            trait.setNameTag(owner);
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
