package io.github.fourmisain.stitch.mixin;

import io.github.fourmisain.stitch.impl.StitchImpl;
import net.minecraft.client.texture.AtlasManager;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

// runs after stitching is done. could be replaced by a ResourceReloader
@Mixin(AtlasManager.Stitch.class)
public abstract class AtlasManagerStitchMixin {
	@Inject(method = "createSpriteMap", at = @At("HEAD"))
	public void clearAnimationResourceMetadata(CallbackInfoReturnable<Map<SpriteIdentifier, Sprite>> cir) {
		StitchImpl.animationResources.clear();
	}
}
