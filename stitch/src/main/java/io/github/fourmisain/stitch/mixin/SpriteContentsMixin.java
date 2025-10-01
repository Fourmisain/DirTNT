package io.github.fourmisain.stitch.mixin;

import io.github.fourmisain.stitch.impl.StitchImpl;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteDimensions;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(SpriteContents.class)
public abstract class SpriteContentsMixin {
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@Inject(method = "<init>(Lnet/minecraft/util/Identifier;Lnet/minecraft/client/texture/SpriteDimensions;Lnet/minecraft/client/texture/NativeImage;Ljava/util/Optional;Ljava/util/List;)V", at = @At("RETURN"))
	public void storeAnimationResourceMetadata(Identifier id, SpriteDimensions dimensions, NativeImage image, Optional<AnimationResourceMetadata> animationResourceMetadata, List<?> additionalMetadata, CallbackInfo ci) {
		StitchImpl.animationResources.put((SpriteContents) (Object) this, animationResourceMetadata);
	}
}
