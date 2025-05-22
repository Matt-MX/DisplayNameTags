package com.mattmx.nametags.config;

import com.mattmx.nametags.entity.trait.Trait;

import java.util.LinkedList;
import java.util.List;

public class StyleFromConfigTrait extends Trait {
    private List<ConfigStyleEntry<?>> entries = new LinkedList<>();

    public StyleFromConfigTrait() {

    }

}
