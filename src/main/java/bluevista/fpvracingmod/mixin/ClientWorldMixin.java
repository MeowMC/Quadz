package bluevista.fpvracingmod.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {
    @Shadow @Final MinecraftClient client;
    @Shadow abstract void doRandomBlockDisplayTicks(int x, int y, int z);

    @Inject(at = @At("HEAD"), method = "doRandomBlockDisplayTicks", cancellable = true)
    public void blockDisplayTicks(int xCenter, int yCenter, int zCenter, CallbackInfo info) {
        Camera cam = client.gameRenderer.getCamera();
        int camX = (int) cam.getPos().getX();
        int camY = (int) cam.getPos().getY();
        int camZ = (int) cam.getPos().getZ();

        if (camX != xCenter && camY != yCenter && camZ != zCenter) {
            doRandomBlockDisplayTicks(camX, camY, camZ);
        }
    }
}
