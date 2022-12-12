package io.github.fourmisain.stitch.mixin;

import com.google.common.collect.ImmutableList;
import io.github.fourmisain.stitch.api.SpriteRecipe;
import io.github.fourmisain.stitch.impl.DummySpriteContents;
import io.github.fourmisain.stitch.impl.StitchImpl;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.*;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static io.github.fourmisain.stitch.impl.DummySpriteContents.DUMMY_ID;

@Mixin(SpriteLoader.class)
public abstract class SpriteLoaderMixin2 {
	// step 1: remember the parameters from the first lambda inside method_47661
	@Inject(method = "method_47660(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/Identifier;)Ljava/util/List", at = @At("HEAD"))
	private static void stitch$captureParameters(ResourceManager resourceManager, Identifier atlasInfoLocation, CallbackInfoReturnable<List<Supplier<SpriteContents>>> cir) {
		StitchImpl.resourceManager.set(resourceManager);
		StitchImpl.atlasId.set(atlasInfoLocation);
	}

	@ModifyVariable(method = "method_47663", at = @At("HEAD"), argsOnly = true)
	public List<SpriteContents> stitch$generateSprites(List<SpriteContents> originalSprites) {
		// step 4: get smuggled goods from our dummy
		// also, step 5: don't forget to remove/ignore the dummy
		Identifier atlasId = null;
		ResourceManager resourceManager = null;

		for (SpriteContents sprite : originalSprites) {
			if (sprite.getId().equals(DUMMY_ID)) {
				DummySpriteContents dummy = (DummySpriteContents) sprite;
				atlasId = dummy.atlasId;
				resourceManager = dummy.resourceManager;
				break;
			}
		}

		// distribute sprite data for all recipes
		Map<Identifier, SpriteRecipe> recipeMap = StitchImpl.atlasRecipes.getOrDefault(atlasId, Map.of());

		for (SpriteContents sprite : originalSprites) {
			if (sprite.getId().equals(DUMMY_ID)) continue;
			for (SpriteRecipe recipe : recipeMap.values()) {
				if (recipe.getDependencies().contains(sprite.getId())) {
					recipe.collectSprite(sprite);
				}
			}
		}

		List<Supplier<SpriteContents>> toGenerate = new ArrayList<>();

		for (var entry : recipeMap.entrySet()) {
			Identifier id = entry.getKey();
			SpriteRecipe recipe = entry.getValue();

			final ResourceManager resourceManager2 = resourceManager;
			toGenerate.add(() -> {
				// actually generate the sprite
				SpriteDimensions size = recipe.generateSize();
				AnimationResourceMetadata animationData = recipe.generateAnimationData();
				NativeImage image = recipe.generateImage(resourceManager2);

				if (image == null) return null; // turn into missing texture

				return new SpriteContents(id, size, image, animationData);
			});
		}

		// generate sprites in another asynchronous step
		// TODO this blocks the current thread, which "works" when there's 10 atlases for 32 workers but the dead lock potential is no joke
		CompletableFuture<List<SpriteContents>> future = SpriteLoader.method_47664(toGenerate, StitchImpl.executor.get());

		List<SpriteContents> generated = future.join();

		// remove dummy and add generated sprites
		return ImmutableList.<SpriteContents>builder()
			.addAll(originalSprites.stream().filter(s -> !s.getId().equals(DUMMY_ID)).iterator())
			.addAll(generated)
			.build();
	}
}
