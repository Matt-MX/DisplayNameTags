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
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class OutgoingPacketListener extends PacketListenerAbstract {
    private static final byte TEXT_DISPLAY_TEXT_INDEX = 23;
    private static final byte PRE_1_20_2_TRANSLATION_INDEX = 10;
    private static final byte POST_1_20_2_TRANSLATION_INDEX = 10;
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

                // Add passenger and send to player after
                event.getTasksAfterSend().add(() -> {
                    // To avoid name tag moving when being added
                    nameTagEntity.updateLocation();

                    nameTagEntity.getPassenger().removeViewer(event.getUser());
                    nameTagEntity.getPassenger().addViewer(event.getUser());

                    event.getUser().sendPacket(nameTagEntity.getPassengersPacket());
                });
            }
            case PacketType.Play.Server.ENTITY_METADATA -> {
                WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(event);

                NameTagEntity nameTagEntity = plugin.getEntityManager().getNameTagEntityByTagEntityId(packet.getEntityId());

                if (nameTagEntity == null) return;

                // Backwards compatibility for clients older than 1.20.2
                // Mojank changed the passenger origin point when riding an entity so the tag appears inside their head.
                if (event.getUser().getClientVersion().isOlderThan(ClientVersion.V_1_20_2)) {

                    byte index = PacketEvents.getAPI()
                        .getServerManager()
                        .getVersion()
                        .is(VersionComparison.OLDER_THAN, ServerVersion.V_1_20_2)
                        ? PRE_1_20_2_TRANSLATION_INDEX
                        : POST_1_20_2_TRANSLATION_INDEX;

                    packet.getEntityMetadata()
                        .stream()
                        .filter((meta) -> meta.getIndex() == index)
                        .findFirst()
                        .ifPresentOrElse((data) -> {
                                Vector3f vec = (Vector3f) data.getValue();
                                data.setValue(vec.add(PRE_1_20_2_TRANSLATION_OFFSET));
                            }, () -> packet.getEntityMetadata().add(new EntityData(
                                index,
                                EntityDataTypes.VECTOR3F,
                                PRE_1_20_2_TRANSLATION_OFFSET
                            ))
                        );
                    event.markForReEncode(true);
                }

                // Apply relational placeholders to the text of an outgoing display entity
                if (nameTagEntity.getBukkitEntity() instanceof Player from) {
                    packet.getEntityMetadata()
                        .stream()
                        .filter((meta) -> meta.getIndex() == TEXT_DISPLAY_TEXT_INDEX && meta.getValue() instanceof Component)
                        .findFirst()
                        .ifPresent((data) -> {
                            final Component originalText = (Component) data.getValue();
                            final Player to = event.getPlayer();

                            // TODO(Matt): Replace use of legacy serializer
                            String legacy = LegacyComponentSerializer
                                .legacyAmpersand()
                                .serialize(originalText);

                            final Component appliedText = LegacyComponentSerializer
                                .legacyAmpersand()
                                .deserialize(PlaceholderAPI.setRelationalPlaceholders(from, to, legacy));

                            if (!originalText.equals(appliedText)) {

                                data.setValue(appliedText);

                                event.markForReEncode(true);
                            }
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
                if (Arrays.stream(packet.getPassengers()).noneMatch((i) -> nameTagEntity.getPassenger().getEntityId() == i)) {

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
