package dev.lazurite.quadz.client.mixin.resource;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.data.util.TemplateTextureManager;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TextureManager.class)
public abstract class TextureManagerMixin {
    @Shadow @Final private ResourceManager resourceContainer;

    @Redirect(
            method = "method_24303",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/texture/TextureManager;resourceContainer:Lnet/minecraft/resource/ResourceManager;"
            )
    )
    public ResourceManager getResourceContainer(TextureManager manager, Identifier identifier, AbstractTexture texture) {
        if (identifier.getNamespace().equals(Quadz.MODID)) {
            return new TemplateTextureManager(resourceContainer);
        }

        return resourceContainer;
    }
}
