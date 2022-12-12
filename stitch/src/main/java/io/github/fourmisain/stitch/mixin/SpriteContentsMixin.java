package io.github.fourmisain.stitch.mixin;

import io.github.fourmisain.stitch.impl.AnimationDataAccess;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteDimensions;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteContents.class)
public class SpriteContentsMixin implements AnimationDataAccess {
	private AnimationResourceMetadata stitch$animationData;

	@Inject(method = "<init>", at = @At("RETURN"))
	public void f(Identifier id, SpriteDimensions dimensions, NativeImage image, AnimationResourceMetadata animationData, CallbackInfo ci) {
		stitch$animationData = animationData;
	}

	@Override
	public AnimationResourceMetadata getAnimationData() {
		return stitch$animationData;
	}

	@Override
	public void clearAnimationData() {
		stitch$animationData = null;
	}
}
