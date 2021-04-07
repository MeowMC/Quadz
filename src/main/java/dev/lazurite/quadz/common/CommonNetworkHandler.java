package dev.lazurite.quadz.common;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.input.Mode;
import dev.lazurite.quadz.common.data.DataDriver;
import dev.lazurite.quadz.common.data.model.Template;
import dev.lazurite.quadz.common.state.Bindable;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.util.input.InputFrame;
import dev.lazurite.quadz.common.state.QuadcopterState;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;

import java.util.function.Consumer;

public class CommonNetworkHandler {
    public static void onTemplateReceived(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        Template template = Template.deserialize(buf);

        server.execute(() -> {
            PlayerLookup.all(server).forEach(p -> {
                if (!p.equals(player)) {
                    ServerPlayNetworking.send(p, Quadz.TEMPLATE, template.serialize());
                }
            });

            DataDriver.load(template);
        });
    }

    public static void onNoClipKey(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        server.execute(() -> {
            Bindable.get(player.getMainHandStack()).ifPresent(transmitter -> {
                QuadcopterEntity quadcopter = QuadcopterState.findQuadcopter(player.getEntityWorld(), player.getCameraEntity().getPos(), transmitter.getBindId(), server.getPlayerManager().getViewDistance());

                if (quadcopter != null) {
                    boolean lastNoClip = quadcopter.getRigidBody().shouldDoTerrainLoading();
                    quadcopter.getRigidBody().setDoTerrainLoading(!lastNoClip);
                    quadcopter.getRigidBody().setDoEntityLoading(!lastNoClip);

                    if (lastNoClip) {
                        player.sendMessage(new TranslatableText("message.quadz.noclip_on"), true);
                    } else {
                        player.sendMessage(new TranslatableText("message.quadz.noclip_off"), true);
                    }
                }
            });
        });
    }

    public static void onChangeCameraAngleKey(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int amount = buf.readInt();

        server.execute(() -> {
            Bindable.get(player.getMainHandStack()).ifPresent(transmitter -> {
                if (player.getCameraEntity() instanceof QuadcopterEntity) {
                    QuadcopterEntity quadcopter = (QuadcopterEntity) player.getCameraEntity();

                    if (quadcopter.isBoundTo(transmitter)) {
                        quadcopter.setCameraAngle(quadcopter.getCameraAngle() + amount);
                    }
                } else {
                    QuadcopterEntity quadcopter = QuadcopterState.findQuadcopter(player.getEntityWorld(), player.getCameraEntity().getPos(), transmitter.getBindId(), server.getPlayerManager().getViewDistance());

                    if (quadcopter != null) {
                        quadcopter.setCameraAngle(quadcopter.getCameraAngle() + amount);
                    }
                }
            });
        });
    }

    public static void onPowerGogglesKey(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        boolean enable = buf.readBoolean();

        server.execute(() -> {
            ItemStack hat = player.inventory.armor.get(3);
            ItemStack hand = player.inventory.getMainHandStack();
            ItemStack goggles = null;

            if (hand.getItem().equals(Quadz.GOGGLES_ITEM)) {
                goggles = hand;
            } else if (hat.getItem().equals(Quadz.GOGGLES_ITEM)) {
                goggles = hat;
            }

            if (goggles != null) {
                goggles.getOrCreateTag().putBoolean("enabled", enable);
            }

            player.playSound(enable ? SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON : SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF, 1.0f, 1.0f);
        });
    }

    public static void onGodModeKey(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        server.execute(() -> {
            ItemStack hand = player.getMainHandStack();

            Consumer<QuadcopterState> changeGodMode = quadcopter -> {
                quadcopter.setGodMode(!quadcopter.isInGodMode());

                if (quadcopter.isInGodMode()) {
                    player.sendMessage(new TranslatableText("message.quadz.godmode_on"), true);
                } else {
                    player.sendMessage(new TranslatableText("message.quadz.godmode_off"), true);
                }
            };

            QuadcopterState.get(hand).ifPresent(changeGodMode);
            Bindable.get(hand).ifPresent(transmitter ->
                changeGodMode.accept(QuadcopterState.findQuadcopter(player.getEntityWorld(), player.getCameraEntity().getPos(), transmitter.getBindId(), server.getPlayerManager().getViewDistance())));
        });
    }

    public static void onInputFrame(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int entityId = buf.readInt();
        InputFrame frame = new InputFrame(
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readEnumConstant(Mode.class));

        server.execute(() -> {
            Bindable.get(player.getMainHandStack()).ifPresent(transmitter -> {
                Entity entity = player.getEntityWorld().getEntityById(entityId);

                if (entity instanceof QuadcopterEntity) {
                    if (((QuadcopterEntity) entity).isBoundTo(transmitter)) {
                        ((QuadcopterEntity) entity).getInputFrame().set(frame);
                    }
                }
            });
        });
    }
}