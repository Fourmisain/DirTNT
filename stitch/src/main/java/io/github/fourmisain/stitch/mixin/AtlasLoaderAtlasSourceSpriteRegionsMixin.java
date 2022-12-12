package io.github.fourmisain.stitch.mixin;

import io.github.fourmisain.stitch.impl.StitchImpl;
import net.minecraft.client.texture.atlas.AtlasSource;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

// skip adding the vanilla sprite suppliers, can be used to replace vanilla textures
@Mixin(targets = "net.minecraft.client.texture.atlas.AtlasLoader$1")
public abstract class AtlasLoaderAtlasSourceSpriteRegionsMixin {
	@Inject(method = "add", at = @At("HEAD"), cancellable = true)
	public void stitch$skipVanillaSupplier(Identifier id, AtlasSource.SpriteRegion region, CallbackInfo ci) {
		var recipeMap = StitchImpl.atlasRecipes.getOrDefault(StitchImpl.atlasId.get(), Map.of());
		if (recipeMap.containsKey(id)) {
			ci.cancel();
		}
	}
}
