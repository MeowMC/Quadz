package dev.lazurite.fpvracing.common.tick;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.common.entity.QuadcopterEntity;
import dev.lazurite.fpvracing.common.item.container.GogglesContainer;
import dev.lazurite.fpvracing.common.item.container.TransmitterContainer;
import dev.lazurite.transporter.impl.pattern.part.Quad;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class GogglesTick {
    public static void tick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            Optional<GogglesContainer> goggles = FPVRacing.GOGGLES_CONTAINER.maybeGet(player.inventory.armor.get(3));

            /* Goggles enabled, enter a quadcopter view if one is nearby */
            if (goggles.isPresent() && goggles.get().isEnabled()) {
                if (!(player.getCameraEntity() instanceof QuadcopterEntity)) {
                    List<QuadcopterEntity> quads = player.getEntityWorld().getEntitiesByClass(QuadcopterEntity.class, new Box(player.getBlockPos()).expand(goggles.get().getRange()), EntityPredicates.VALID_ENTITY);

                    if (!quads.isEmpty()) {
                        QuadcopterEntity entity = quads.get(0);

                        if (entity != null) {
                            Optional<TransmitterContainer> transmitter = FPVRacing.TRANSMITTER_CONTAINER.maybeGet(player.getMainHandStack());
                            player.setCameraEntity(entity);

                            if (transmitter.isPresent()) {
                                if (entity.getBindId() == transmitter.get().getBindId()) {
                                    entity.getRigidBody().prioritize(player);
                                }
                            }
                        }
                    }
                }

            /* Goggles disabled and player is in quadcopter view, reset view */
            } else if (player.getCameraEntity() instanceof QuadcopterEntity){
                QuadcopterEntity entity = (QuadcopterEntity) player.getCameraEntity();

                if (player.equals(entity.getRigidBody().getPriorityPlayer())) {
                    entity.getRigidBody().prioritize(null);
                }

                player.setCameraEntity(player);
            }
        }
    }
}
