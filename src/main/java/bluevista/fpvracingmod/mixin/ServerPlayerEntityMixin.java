package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.server.ServerTick;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.SetCameraEntityS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Shadow Entity cameraEntity;
    @Shadow ServerPlayNetworkHandler networkHandler;

    @Inject(at = @At("HEAD"), method = "onStoppedTracking", cancellable = true)
    public void onStoppedTracking(Entity entity, CallbackInfo info) {
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) entity;

            if (ServerTick.isInGoggles(player)) {
                info.cancel();
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "setCameraEntity", cancellable = true)
    public void setCameraEntity(Entity entity, CallbackInfo info) {
        Entity prevEntity = cameraEntity;
        Entity nextEntity = (Entity) (entity == null ? this : entity);

        if (prevEntity instanceof DroneEntity || nextEntity instanceof DroneEntity) {
            networkHandler.sendPacket(new SetCameraEntityS2CPacket(nextEntity));
            cameraEntity = nextEntity;
            info.cancel();
        }
    }

    @Inject(at = @At("TAIL"), method = "onDisconnect")
    public void onDisconnect(CallbackInfo info) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        if (ServerTick.isInGoggles(player)) {
            ServerTick.resetView(player);
        }
    }

    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;shouldDismount()Z")
    )
    public boolean shouldDismount(ServerPlayerEntity player) {
        if (ServerTick.isInGoggles(player)) {
            return false;
        } else {
            return player.isSneaking();
        }
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;updatePositionAndAngles(DDDFF)V",
                    ordinal = 0
            )
    )
    public void updatePositionAndAngles(ServerPlayerEntity entity, double x, double y, double z, float yaw, float pitch) {
//         Just DONT move the player anymore plz
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;setCameraEntity(Lnet/minecraft/entity/Entity;)V",
                    ordinal = 1
            )
    )
    public void tick(CallbackInfo info) {
        ServerTick.resetView((ServerPlayerEntity) (Object) this);
    }
}
