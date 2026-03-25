package io.github.fourmisain.stitch.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.fourmisain.stitch.impl.StitchImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.Identifier;

@Mixin(SpriteContents.class)
public abstract class SpriteContentsMixin {
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@Inject(method = "<init>(Lnet/minecraft/resources/Identifier;Lnet/minecraft/client/resources/metadata/animation/FrameSize;Lcom/mojang/blaze3d/platform/NativeImage;Ljava/util/Optional;Ljava/util/List;Ljava/util/Optional;)V", at = @At("RETURN"))
	public void storeAnimationResourceMetadata(Identifier id, FrameSize frameSize, NativeImage image, Optional<AnimationMetadataSection> animationMetadata, List<?> additionalMetadata, Optional<TextureMetadataSection> textureMetadata, CallbackInfo ci) {
		var self = (SpriteContents) (Object) this;
		StitchImpl.animationResources.put(self, animationMetadata);
		StitchImpl.textureResources.put(self, textureMetadata);
	}
}
