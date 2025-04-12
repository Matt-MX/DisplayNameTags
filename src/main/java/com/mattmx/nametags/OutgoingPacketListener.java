package com.mattmx.nametags;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.manager.server.VersionComparison;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.mattmx.nametags.entity.NameTagEntity;
import com.mattmx.nametags.hook.PapiHook;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class OutgoingPacketListener extends PacketListenerAbstract {
    private static final byte TEXT_DISPLAY_TEXT_INDEX = 23;
    private static final byte PRE_1_20_2_TRANSLATION_INDEX = 10;
    private static final byte POST_1_20_2_TRANSLATION_INDEX = 11;
    private static final byte ENTITY_OFFSET_INDEX = PacketEvents.getAPI()
            .getServerManager()
            .getVersion()
            .is(VersionComparison.OLDER_THAN, ServerVersion.V_1_20_2)
            ? PRE_1_20_2_TRANSLATION_INDEX
            : POST_1_20_2_TRANSLATION_INDEX;
    private static final Vector3f PRE_1_20_2_TRANSLATION_OFFSET = new Vector3f(0f, 0.4f, 0f);
    private final @NotNull NameTags plugin;

    public OutgoingPacketListener(@NotNull NameTags plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPacketSend(@NotNull PacketSendEvent event) {
        switch (event.getPacketType()) {
            case PacketType.Play.Server.SPAWN_ENTITY -> {
                WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity(event);

                if (packet.getUUID().isEmpty()) return;

                NameTagEntity nameTagEntity = plugin.getEntityManager().getNameTagEntityByUUID(packet.getUUID().get());

                if (nameTagEntity == null) return;

                // Add passenger and send to player after (off the netty thread)
                final PacketSendEvent clone = event.clone();
                event.getTasksAfterSend().add(() -> plugin.getExecutor().execute(() -> {
                    // To avoid name tag moving when being added
                    nameTagEntity.updateLocation();

                    // Refreshes as viewer (crusty fix)
                    nameTagEntity.getPassenger().removeViewer(clone.getUser());
                    nameTagEntity.getPassenger().addViewer(clone.getUser());

                    clone.getUser().sendPacket(nameTagEntity.getPassengersPacket());
                }));
            }
            case PacketType.Play.Server.ENTITY_METADATA -> {
                WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(event);

                NameTagEntity nameTagEntity = plugin.getEntityManager().getNameTagEntityByTagEntityId(packet.getEntityId());

                if (nameTagEntity == null) return;

                boolean isOldClient = event.getUser().getClientVersion().isOlderThan(ClientVersion.V_1_20_2);
                boolean containsEntityOffset = false;
                @Nullable EntityData textEntry = null;

                for (final EntityData entry : packet.getEntityMetadata()) {
                    if (containsEntityOffset && textEntry != null) {
                        break;
                    }

                    if (isOldClient && entry.getIndex() == ENTITY_OFFSET_INDEX) {
                        Vector3f vec = (Vector3f) entry.getValue();
                        entry.setValue(vec.add(PRE_1_20_2_TRANSLATION_OFFSET));

                        containsEntityOffset = true;
                        event.markForReEncode(true);
                    } else if (entry.getIndex() == TEXT_DISPLAY_TEXT_INDEX) {
                        textEntry = entry;
                        event.markForReEncode(true);
                    }
                }

                // Backwards compatibility for clients older than 1.20.2
                // Mojank changed the passenger origin point when riding an entity so the tag appears inside their head.
                if (isOldClient) {
                    // If there was no offset found then add one ourselves for the offset.
                    if (!containsEntityOffset) {
                        packet.getEntityMetadata().add(new EntityData(
                                ENTITY_OFFSET_INDEX,
                                EntityDataTypes.VECTOR3F,
                                PRE_1_20_2_TRANSLATION_OFFSET
                        ));
                        event.markForReEncode(true);
                    }
                }

                // Apply relational placeholders to the text of an outgoing display entity
                if (plugin.getConfig().getBoolean("options.relative-placeholders-support") &&
                        nameTagEntity.getBukkitEntity() instanceof Player from &&
                        textEntry != null
                ) {
                    final Component originalText = (Component) textEntry.getValue();
                    final Player to = event.getPlayer();

                    // TODO(Matt): Replace use of legacy serializer
                    String legacy = LegacyComponentSerializer
                            .legacyAmpersand()
                            .serialize(originalText);

                    // If it doesn't have any placeholders in then stop
                    if (!legacy.contains("%rel_")) break;

                    // Cancel it since we want to deserialize off the netty thread and then send the packet.
                    event.setCancelled(true);

                    final PacketSendEvent clone = event.clone();
                    final EntityData finalTextEntry = textEntry;
                    plugin.getExecutor().execute(() -> {
                        final String appliedString = PapiHook.setRelationalPlaceholders(from, to, legacy)
                                .replaceAll("%rel_[a-zA-Z0-9_ ]%", "");

                        final Component appliedText = LegacyComponentSerializer
                                .legacyAmpersand()
                                .deserialize(appliedString);

                        finalTextEntry.setValue(appliedText);
                        clone.getUser().sendPacket(packet);
                    });
                }
            }
            case PacketType.Play.Server.DESTROY_ENTITIES -> {
                WrapperPlayServerDestroyEntities packet = new WrapperPlayServerDestroyEntities(event);

                for (int entityId : packet.getEntityIds()) {
                    NameTagEntity nameTagEntity = plugin.getEntityManager().getNameTagEntityById(entityId);

                    if (nameTagEntity == null) continue;

                    nameTagEntity.getPassenger().removeViewer(event.getUser());
                }
            }
            case PacketType.Play.Server.ENTITY_EFFECT -> {
                // TODO per-player impl
                final WrapperPlayServerEntityEffect packet = new WrapperPlayServerEntityEffect(event);

                if (packet.getPotionType() != PotionTypes.INVISIBILITY) return;

                final NameTagEntity nameTagEntity = plugin.getEntityManager().getNameTagEntityById(packet.getEntityId());

                if (nameTagEntity == null) return;

                nameTagEntity.updateVisibility(true);
            }
            case PacketType.Play.Server.REMOVE_ENTITY_EFFECT -> {
                // TODO per-player impl
                final WrapperPlayServerRemoveEntityEffect packet = new WrapperPlayServerRemoveEntityEffect(event);

                if (packet.getPotionType() != PotionTypes.INVISIBILITY) return;

                final NameTagEntity nameTagEntity = plugin.getEntityManager().getNameTagEntityById(packet.getEntityId());

                if (nameTagEntity == null) return;

                nameTagEntity.updateVisibility(false);
            }
            case PacketType.Play.Server.SET_PASSENGERS -> {
                final WrapperPlayServerSetPassengers packet = new WrapperPlayServerSetPassengers(event);

                final NameTagEntity nameTagEntity = plugin.getEntityManager().getNameTagEntityById(packet.getEntityId());

                if (nameTagEntity == null) return;

                // If the packet doesn't already contain our entity
                boolean containsNameTagPassenger = false;
                for (final int passengerId : packet.getPassengers()) {
                    if (passengerId == nameTagEntity.getPassenger().getEntityId()) {
                        containsNameTagPassenger = true;
                    }
                }

                // TODO(Matt): Should we process async and then send another passenger packet afterwards?
                if (!containsNameTagPassenger) {

                    // Add our entity
                    int[] passengers = Arrays.copyOf(packet.getPassengers(), packet.getPassengers().length + 1);
                    passengers[passengers.length - 1] = nameTagEntity.getPassenger().getEntityId();

                    packet.setPassengers(passengers);

                    NameTags.getInstance()
                            .getEntityManager()
                            .setLastSentPassengers(packet.getEntityId(), passengers);

                    event.markForReEncode(true);
                }
            }
            default -> {
            }
        }
    }
}
