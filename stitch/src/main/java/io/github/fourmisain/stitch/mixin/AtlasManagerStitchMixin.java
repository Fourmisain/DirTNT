package io.github.fourmisain.stitch.mixin;

import io.github.fourmisain.stitch.impl.StitchImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.AtlasManager;
import net.minecraft.client.resources.model.Material;

// runs after stitching is done. could be replaced by a ResourceReloader
@Mixin(AtlasManager.PendingStitchResults.class)
public abstract class AtlasManagerStitchMixin {
	@Inject(method = "joinAndUpload", at = @At("HEAD"))
	public void clearAnimationResourceMetadata(CallbackInfoReturnable<Map<Material, TextureAtlasSprite>> cir) {
		StitchImpl.animationResources.clear();
		StitchImpl.textureResources.clear();
	}
}
