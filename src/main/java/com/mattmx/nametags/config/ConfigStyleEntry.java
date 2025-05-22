package com.mattmx.nametags.config;

import com.mattmx.nametags.entity.NameTagHolder;
import me.tofaa.entitylib.wrapper.WrapperEntity;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class ConfigStyleEntry<T> {
    public String key;
    public BiFunction<Object, NameTagHolder, T> supplier;
    public BiConsumer<T, WrapperEntity> apply;

    public ConfigStyleEntry(
            String key,
            BiFunction<Object, NameTagHolder, T> supplier,
            BiConsumer<T, WrapperEntity> apply
    ) {
        this.key = key;
        this.supplier = supplier;
        this.apply = apply;
    }
}
