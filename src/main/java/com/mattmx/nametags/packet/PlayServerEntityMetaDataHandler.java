package com.mattmx.nametags.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.manager.server.VersionComparison;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.mattmx.nametags.NameTags;
import com.mattmx.nametags.entity.NameTagEntity;
import com.mattmx.nametags.hook.PapiHook;
import com.mattmx.nametags.utils.ComponentUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Responsible for two things:
 * <p>
 * 1. Modifying the passenger entity Y offset, since Mojang changed the
 * entity passenger origin by a small amount, which results in the name
 * tags rendering inside a player's head in older versions.
 * <p>
 * To fix this, the function will apply an offset of +0.4f in the Y
 * axis, which was the closest value found to how it should appear in
 * modern versions.
 * <p>
 * 2. Apply relational placeholders (off the netty thread) if there are
 * any.
 */
public class PlayServerEntityMetaDataHandler {
    private static final byte TEXT_DISPLAY_TEXT_INDEX = 23;
    private static final byte PRE_1_20_2_TRANSLATION_INDEX = 10;
    private static final byte POST_1_20_2_TRANSLATION_INDEX = 11;

    private static final Vector3f PRE_1_20_2_TRANSLATION_OFFSET = new Vector3f(0f, 0.4f, 0f);
    private static final byte ENTITY_OFFSET_INDEX = PacketEvents.getAPI()
        .getServerManager()
        .getVersion()
        .is(VersionComparison.OLDER_THAN, ServerVersion.V_1_20_2)
        ? PRE_1_20_2_TRANSLATION_INDEX
        : POST_1_20_2_TRANSLATION_INDEX;

    private static final TextComponent RELATIVE_ARG_PREFIX = Component.text("%rel_");

    public static void handlePacket(@NotNull PacketSendEvent event) {
        final NameTags plugin = NameTags.getInstance();

        final PacketSendEvent eventClone = event.clone();
        final WrapperPlayServerEntityMetadata packet0 = new WrapperPlayServerEntityMetadata(event);

        final NameTagEntity nameTagEntity = plugin.getEntityManager().getNameTagEntityByTagEntityId(packet0.getEntityId());

        if (nameTagEntity == null) {
            eventClone.cleanUp();
            return;
        }

        event.setCancelled(true);
        final WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(eventClone);

        // This could prove a concurrency issue, maybe we should keep track of if there is a newer packet processing?
        plugin.getExecutor().execute(() -> {
            boolean isOldClient = eventClone.getUser()
                .getClientVersion()
                .isOlderThan(ClientVersion.V_1_20_2);

            boolean containsEntityOffset = false;
            @Nullable EntityData textEntry = null;

            for (final EntityData entry : packet.getEntityMetadata()) {
                if (containsEntityOffset && textEntry != null) {
                    break;
                }

                if (isOldClient && entry.getIndex() == ENTITY_OFFSET_INDEX) {
                    Vector3f vec = (Vector3f) entry.getValue();
                    // If there is already an entity offset, and it's an old client, add to it.
                    entry.setValue(vec.add(PRE_1_20_2_TRANSLATION_OFFSET));

                    containsEntityOffset = true;
                } else if (entry.getIndex() == TEXT_DISPLAY_TEXT_INDEX) {
                    textEntry = entry;
                }
            }

            // Backwards compatibility for clients older than 1.20.2
            // Mojank changed the passenger origin point when riding an entity so the tag appears inside their head.
            if (isOldClient && !containsEntityOffset) {
                // If there was no offset found then add one ourselves for the offset.
                packet.getEntityMetadata().add(new EntityData(
                    ENTITY_OFFSET_INDEX,
                    EntityDataTypes.VECTOR3F,
                    PRE_1_20_2_TRANSLATION_OFFSET
                ));
            }

            // Apply relational placeholders to the text of an outgoing display entity
            if (plugin.getConfig().getBoolean("options.relative-placeholders-support") &&
                nameTagEntity.getBukkitEntity() instanceof Player from &&
                textEntry != null
            ) {
                final TextComponent originalText = (TextComponent) textEntry.getValue();
                final Player to = eventClone.getPlayer();

                boolean containsRelativePlaceholder = ComponentUtils.startsWith(originalText, RELATIVE_ARG_PREFIX);

                // If it doesn't have any placeholders in then stop
                if (!containsRelativePlaceholder) {
                    eventClone.getUser().sendPacketSilently(packet);
                    return;
                }

                final Component textWithRelativeApplied = PapiHook.setRelationalPlaceholders(from, to, originalText);

                textEntry.setValue(textWithRelativeApplied);
                eventClone.getUser().sendPacketSilently(packet);
            } else {
                eventClone.getUser().sendPacketSilently(packet);
            }
        });
    }

}
