package com.mattmx.nametags.defaults;

import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.config.Settings;
import com.mattmx.nametags.config.TagGroup;
import com.mattmx.nametags.display.TagMeta;
import com.mattmx.nametags.entity.NameTagEntity;
import com.mattmx.nametags.entity.trait.RefreshTrait;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

final class DefaultsRefresh implements Consumer<NameTagEntity> {
    private final NameTags plugin;
    private final Player player;
    private final TagText text = new TagText();
    private boolean primed;

    DefaultsRefresh(@NotNull NameTags plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    static void start(@NotNull NameTags plugin, @NotNull NameTagEntity tag, @NotNull Player player, long periodMillis) {
        DefaultsRefresh refresh = new DefaultsRefresh(plugin, player);
        tag.getTraits().getOrAddTrait(RefreshTrait.class, () -> RefreshTrait.ofMillis(plugin, periodMillis, refresh));
    }

    @Override
    public void accept(@NotNull NameTagEntity tag) {
        if (primed && tag.getPassenger().getViewers().isEmpty()) {
            return;
        }
        primed = true;
        Settings settings = plugin.getSettings();
        TagGroup group = settings.groupFor(player);
        TagMeta meta = tag.getMeta();

        group.applyTo(meta, settings.defaults().style());
        Component built = text.build(settings.linesFor(group), player, settings);
        if (built != null) {
            meta.setText(built);
        }
        tag.reconcileFlags(player.isSneaking(), tag.isInvisible());
        tag.getPassenger().refresh();

        long period = settings.refreshFor(group);
        RefreshTrait current = tag.getTraits().getTraitOrNull(RefreshTrait.class);
        if (current != null && current.getPeriodMillis() != period) {
            tag.getTraits().removeTrait(RefreshTrait.class);
            start(plugin, tag, player, period);
        }
    }
}
