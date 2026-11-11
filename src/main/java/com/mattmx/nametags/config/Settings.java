package com.mattmx.nametags.config;

import com.mattmx.nametags.hook.PapiHook;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record Settings(
    boolean defaultsEnabled,
    @NotNull TagGroup defaults,
    @NotNull List<TagGroup> groups,
    boolean placeholderApi,
    boolean relationalPlaceholders,
    boolean removeEmptyLines,
    boolean showSelf,
    boolean showSelfBedrock,
    boolean sneakEnabled,
    int sneakOpacity,
    boolean disableVanilla,
    @NotNull TextFormatter formatter
) {
    private static final long DEFAULT_REFRESH = 50L;

    public static @NotNull Settings load(@NotNull FileConfiguration config) {
        ConfigurationSection defaultsSection = config.getConfigurationSection("defaults");
        if (defaultsSection == null) {
            defaultsSection = config.createSection("defaults");
        }
        TagGroup defaults = new TagGroup(
            "",
            Integer.MIN_VALUE,
            TagStyle.parse(defaultsSection),
            List.copyOf(defaultsSection.getStringList("text")),
            defaultsSection.getLong("refresh-every", DEFAULT_REFRESH)
        );

        List<TagGroup> groups = new ArrayList<>();
        ConfigurationSection groupsSection = config.getConfigurationSection("groups");
        if (groupsSection != null) {
            for (String key : groupsSection.getKeys(false)) {
                ConfigurationSection section = groupsSection.getConfigurationSection(key);
                if (section == null) {
                    continue;
                }
                groups.add(new TagGroup(
                    "nametags.groups." + key,
                    section.getInt("priority"),
                    TagStyle.parse(section),
                    List.copyOf(section.getStringList("text")),
                    section.getLong("refresh-every", -1L)
                ));
            }
        }
        groups.sort(Comparator.comparingInt(TagGroup::priority).reversed());

        boolean placeholderApi = PapiHook.isPapi();
        return new Settings(
            defaultsSection.getBoolean("enabled"),
            defaults,
            List.copyOf(groups),
            placeholderApi,
            config.getBoolean("options.relative-placeholders-support") && placeholderApi,
            defaultsSection.getBoolean("remove-empty-lines", false),
            config.getBoolean("show-self", false),
            config.getBoolean("show-self-bedrock", false),
            config.getBoolean("sneak.enabled"),
            config.getInt("sneak.opacity", 70),
            config.getBoolean("disable-vanilla", true),
            TextFormatter.getById(config.getString("formatter", "minimessage")).orElse(TextFormatter.MINI_MESSAGE)
        );
    }

    public @NotNull TagGroup groupFor(@NotNull Player player) {
        for (TagGroup group : groups) {
            if (player.hasPermission(group.permission())) {
                return group;
            }
        }
        return defaults;
    }

    public @NotNull List<String> linesFor(@NotNull TagGroup group) {
        return group.lines().isEmpty() ? defaults.lines() : group.lines();
    }

    public long refreshFor(@NotNull TagGroup group) {
        return group.refreshEvery() > 0 ? group.refreshEvery() : defaults.refreshEvery();
    }
}
