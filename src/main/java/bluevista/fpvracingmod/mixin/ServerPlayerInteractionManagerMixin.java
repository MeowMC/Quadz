package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.server.items.DroneSpawnerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Mixin class for {@link ServerPlayerInteractionManager}.
 * @author Ethan Johnson
 */
@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    /**
     * Redirects the {@link ItemStack#setCount(int)} method call in
     * {@link ServerPlayerInteractionManager#interactBlock(ServerPlayerEntity, World, ItemStack, Hand, BlockHitResult)}
     * in order to allow the count to reach zero in creative mode.
     * @param itemStack the {@link ItemStack} on which {@link ItemStack#setCount(int)} was called on
     * @param count the count to set the {@link ItemStack} to
     */
    @Redirect(
            method = "interactBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;setCount(I)V"
            )
    )
    public void setCount(ItemStack itemStack, int count) {
        int realCount = itemStack.getCount();

        if(realCount < 1) {
            itemStack.setCount(1);
        }

        if(itemStack.getItem() instanceof DroneSpawnerItem) {
            itemStack.setCount(realCount);
        } else {
            itemStack.setCount(count);
        }
    }
}
