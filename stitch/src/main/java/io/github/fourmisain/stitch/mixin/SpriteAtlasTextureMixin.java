package io.github.fourmisain.stitch.mixin;

import io.github.fourmisain.stitch.api.SpriteRecipe;
import io.github.fourmisain.stitch.impl.StitchImpl;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureStitcher;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.*;
import java.util.stream.Stream;

/*
 * The main texture stitching logic is found inside SpriteAtlasTexture.stitch() and looks like this:
 * 1. call loadSprites(ResourceManager resourceManager, Set<Identifier> ids) to get a Collection<Sprite.Info>,
 *    where Sprite.Infos only contain size/animation data
 * 2. call textureStitcher.stitch(), which lays out/reserves spots in the texture atlas
 * 3. call `loadSprites(ResourceManager resourceManager, TextureStitcher textureStitcher, int maxLevel)`,
 *    which walks through the stitched atlas via textureStitcher.getStitchedSprites() and *actually* loads
 *    the sprites via `loadSprite()`
 *
 * So to dynamically add a sprite, we could simply add a Sprite.Info and generate the sprite in `loadSprite()`.
 *
 * However, if that sprite depends on other sprites, it gets a little more complicated.
 *
 * The plan:
 * 1. abort loading the Sprite.Info
 * 2. collect all Sprite.Info a recipe depends on
 *  once collected:
 *  3. generate a new Sprite.Info
 *  4. add it to the list and recurse steps 2-4
 * 5. let everything stitch
 * 6. abort loading the sprites
 * 7. collect all Sprites a recipe depends on
 *  once collected:
 *  8. generate a new Sprite
 *  9. add it to the list and recurse steps 7-9
 *
 * TODO: currently steps 7-9 only do a basic pass
 */
@Mixin(SpriteAtlasTexture.class)
public abstract class SpriteAtlasTextureMixin {
	@Shadow
	public abstract Identifier getId();

	@Dynamic("runAsync lambda in loadSprites(Lnet/minecraft/resource/ResourceManager;Ljava/util/Set;)Ljava/util/Collection;")
	@Inject(method = "method_18160", at = @At("HEAD"), cancellable = true, remap = false)
	private void stitch$skipSpriteInfoLoading(Identifier identifier, ResourceManager resourceManager, Queue<Sprite.Info> queue, CallbackInfo ci) {
		Map<Identifier, SpriteRecipe> recipes = StitchImpl.atlasRecipes.get(getId());
		if (recipes == null) return;

		if (recipes.containsKey(identifier)) {
			StitchImpl.LOGGER.debug("skipping loading of sprite info {}", identifier);
			ci.cancel();
		}
	}

	@Inject(method = "loadSprites(Lnet/minecraft/resource/ResourceManager;Ljava/util/Set;)Ljava/util/Collection;",
			at = @At("RETURN"))
	private void stitch$constructSpriteInfo(ResourceManager resourceManager, Set<Identifier> ids, CallbackInfoReturnable<Collection<Sprite.Info>> cir) {
		Map<Identifier, SpriteRecipe> recipes = StitchImpl.atlasRecipes.get(getId());
		if (recipes == null) return;

		Collection<Sprite.Info> spriteInfos = cir.getReturnValue();

		// for each recipe we want to know which requirements have been met and be able to strike those off
		Map<Identifier, Set<Identifier>> recipeDependencies = new HashMap<>();
		for (SpriteRecipe recipe : recipes.values()) {
			recipeDependencies.put(recipe.getSpriteId(), new HashSet<>(recipe.getDependencies()));
		}

		Queue<Sprite.Info> infoToProcess = new ArrayDeque<>(spriteInfos);

		while (!infoToProcess.isEmpty()) {
			Sprite.Info info = infoToProcess.remove();

			// TODO iterate over recipeDependencies instead of recipes.values()?
			for (SpriteRecipe recipe : recipes.values()) {
				Identifier recipeId = recipe.getSpriteId();
				Set<Identifier> dependencies = recipeDependencies.get(recipeId);

				// already done
				if (dependencies == null)
					continue;

				// strike off a found dependency and pass it to the recipe
				if (dependencies.remove(info.getId())) {
					recipe.collectSpriteInfo(info);
				}

				// if recipe requirements are met
				if (dependencies.isEmpty()) {
					// generate the new sprite info
					Sprite.Info newInfo = recipe.generateSpriteInfo();

					StitchImpl.LOGGER.debug("adding generated sprite info {}", newInfo.getId());

					// add it to the list
					spriteInfos.add(newInfo);

					// mark this recipe as done
					recipeDependencies.remove(recipeId);

					// process the new info too
					infoToProcess.add(newInfo);
				}
			}
		}
	}

	@Inject(method = "loadSprite(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/client/texture/Sprite$Info;IIIII)Lnet/minecraft/client/texture/Sprite;",
			at = @At("HEAD"),
			cancellable = true)
	public void stitch$skipSpriteLoading(ResourceManager container, Sprite.Info info, int atlasWidth, int atlasHeight, int maxLevel, int x, int y, CallbackInfoReturnable<Sprite> cir) {
		Map<Identifier, SpriteRecipe> recipes = StitchImpl.atlasRecipes.get(getId());
		if (recipes == null) return;

		if (recipes.containsKey(info.getId())) {
			StitchImpl.LOGGER.debug("skipping loading of sprite {}", info.getId());
			cir.setReturnValue(null);
		}
	}

	@Inject(method = "loadSprite(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/client/texture/Sprite$Info;IIIII)Lnet/minecraft/client/texture/Sprite;",
			at = @At("RETURN"))
	public void stitch$collectSpriteDependencies(ResourceManager container, Sprite.Info info, int atlasWidth, int atlasHeight, int maxLevel, int x, int y, CallbackInfoReturnable<Sprite> cir) {
		Map<Identifier, SpriteRecipe> recipes = StitchImpl.atlasRecipes.get(getId());
		if (recipes == null) return;

		Sprite sprite = cir.getReturnValue();

		for (SpriteRecipe recipe : recipes.values()) {
			if (recipe.getDependencies().contains(info.getId())) {
				recipe.collectSprite(sprite);
			}
		}
	}

	@Inject(method = "stitch(Lnet/minecraft/resource/ResourceManager;Ljava/util/stream/Stream;Lnet/minecraft/util/profiler/Profiler;I)Lnet/minecraft/client/texture/SpriteAtlasTexture$Data;",
			at = @At(
					value = "INVOKE_ASSIGN",
					target = "Lnet/minecraft/client/texture/SpriteAtlasTexture;loadSprites(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/client/texture/TextureStitcher;I)Ljava/util/List;"
			),
			slice = @Slice(
					from = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/TextureStitcher;stitch()V")
			),
			locals = LocalCapture.CAPTURE_FAILHARD)
	private void stitch$generateAndAddSprite(ResourceManager resourceManager, Stream<Identifier> idStream, Profiler profiler, int mipmapLevel, CallbackInfoReturnable<SpriteAtlasTexture.Data> cir, Set<Identifier> set, int i, TextureStitcher textureStitcher, /* ... */ int maxLevel, List<Sprite> sprites) {
		SpriteAtlasTexture atlas = (SpriteAtlasTexture) (Object) this;
		StitchImpl.LOGGER.debug("stitching atlas {}", atlas.getId());

		Map<Identifier, SpriteRecipe> recipes = StitchImpl.atlasRecipes.get(getId());
		if (recipes == null) return;

		for (SpriteRecipe recipe : recipes.values()) {
			// simulate loadSprites() for our new sprites
			textureStitcher.getStitchedSprites((info, atlasWidth, atlasHeight, x, y) -> {
				if (info.getId().equals(recipe.getSpriteId())) {
					StitchImpl.LOGGER.debug("adding sprite: {} {} maxLevel={} atlasWidth={} atlasHeight={} x={} y={}", atlas.getId(), recipe.getSpriteId(), maxLevel, atlasWidth, atlasHeight, x, y);
					Sprite newSprite = recipe.generateSprite(resourceManager, atlas, maxLevel, atlasWidth, atlasHeight, x, y);
					sprites.add(newSprite);
				}
			});
		}
	}
}
