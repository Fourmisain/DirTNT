package fourmisain.dirtnt.mixin.autogen;

import fourmisain.dirtnt.DirTnt;
import fourmisain.dirtnt.api.API;
import fourmisain.dirtnt.api.SpriteRecipe;
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
 *    where Sprite.Infos only contain dimension/animation data
 * 2. call textureStitcher.stitch(), which lays out/reserved spots in the texture atlas
 * 3. call `loadSprites(ResourceManager resourceManager, TextureStitcher textureStitcher, int maxLevel)`,
 *    which walks through the stitched via textureStitcher.getStitchedSprites() and *actually* loads
 *    the sprites via `loadSprite()`
 *
 * So to dynamically add a sprite, we simply add a Sprite.Info and generate the sprite in `loadSprite()`.
 *
 * However, if that sprite depends on other sprites, it gets a little more complicated.
 *
 * The plan:
 * 1. collect all Sprite.Info a recipe depends on
 *  once collected:
 *  2. generate a new Sprite.Info
 *  3. add it to the list and recurse steps 1-3
 * 4. let everything stitch
 * 5. abort the loading of those sprites
 * 6. collect all Sprites a recipe depends on
 *  once collected:
 *  7. generate a new Sprite
 *  8. add it to the list and recurse steps 6-8
 *
 * TODO: currently steps 6-8 only do a basic pass
 */
@Mixin(SpriteAtlasTexture.class)
public abstract class SpriteAtlasTextureMixin {
	@Shadow
	public abstract Identifier getId();

	@Dynamic("runAsync lambda in loadSprites(Lnet/minecraft/resource/ResourceManager;Ljava/util/Set;)Ljava/util/Collection;")
	@Inject(method = "method_18160", at = @At("HEAD"), cancellable = true, remap = false)
	private void dirtnt$skipSpriteInfoLoading(Identifier identifier, ResourceManager resourceManager, Queue<Sprite.Info> queue, CallbackInfo ci) {
		List<SpriteRecipe> recipes = API.recipeMap.get(getId());
		if (recipes == null) return;

		// TODO use a Set afterall?
		for (var recipe : recipes) {
			if (recipe.getSpriteId().equals(identifier)) {
				DirTnt.LOGGER.debug("skip loading sprite info {}", recipe.getSpriteId());
				ci.cancel();
			}
		}
	}

	@Inject(method = "loadSprites(Lnet/minecraft/resource/ResourceManager;Ljava/util/Set;)Ljava/util/Collection;",
			at = @At("RETURN"))
	private void dirtnt$constructSpriteInfo(ResourceManager resourceManager, Set<Identifier> ids, CallbackInfoReturnable<Collection<Sprite.Info>> cir) {
		List<SpriteRecipe> recipes = API.recipeMap.get(getId());
		if (recipes == null) return;

		Collection<Sprite.Info> spriteInfos = cir.getReturnValue();

		// for each recipe we want to know which requirements have been met and be able to strike those off
		Map<Identifier, Set<Identifier>> dependencyMap = new HashMap<>();
		for (var recipe : recipes) {
			dependencyMap.put(recipe.getSpriteId(), new HashSet<>(recipe.getDependencies()));
		}

		Queue<Sprite.Info> infoToProcess = new ArrayDeque<>(spriteInfos); // took 211821 ns

		while (!infoToProcess.isEmpty()) {
			Sprite.Info info = infoToProcess.remove();

			for (var recipe : recipes) {
				Identifier recipeId = recipe.getSpriteId();
				Set<Identifier> dependencies = dependencyMap.get(recipeId);

				if (dependencies == null) continue; // already done

				// strike off a found dependency and pass it to the recipe
				if (dependencies.remove(info.getId())) {
					recipe.collectSpriteInfo(info);
				}

				// if recipe requirements are met
				if (dependencies.isEmpty()) {
					Sprite.Info newInfo = recipe.generateSpriteInfo();

					DirTnt.LOGGER.debug("adding sprite info {}", newInfo.getId());

					// add new sprite info
					spriteInfos.add(newInfo);

					// mark this recipe as done
					dependencyMap.remove(recipeId);

					// process the new info too
					infoToProcess.add(newInfo);
				}
			}
		}

		// took 2327307 ns
	}

	@Inject(method = "loadSprite(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/client/texture/Sprite$Info;IIIII)Lnet/minecraft/client/texture/Sprite;",
			at = @At("HEAD"),
			cancellable = true)
	public void dirtnt$skipSpriteLoading(ResourceManager container, Sprite.Info info, int atlasWidth, int atlasHeight, int maxLevel, int x, int y, CallbackInfoReturnable<Sprite> cir) {
		List<SpriteRecipe> recipes = API.recipeMap.get(getId());
		if (recipes == null) return;

		// TODO use a Set afterall?
		for (var recipe : recipes) {
			if (recipe.getSpriteId().equals(info.getId())) {
				DirTnt.LOGGER.debug("skip loading sprite {}", recipe.getSpriteId());
				cir.setReturnValue(null);
			}
		}
	}


	@Inject(method = "loadSprite(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/client/texture/Sprite$Info;IIIII)Lnet/minecraft/client/texture/Sprite;",
			at = @At("RETURN"))
	public void dirtnt$collectSpriteDependencies(ResourceManager container, Sprite.Info info, int atlasWidth, int atlasHeight, int maxLevel, int x, int y, CallbackInfoReturnable<Sprite> cir) {
		List<SpriteRecipe> recipes = API.recipeMap.get(getId());
		if (recipes == null) return;

		Sprite sprite = cir.getReturnValue();

		for (var recipe : recipes) {
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
	private void dirtnt$generateAndAddSprite(ResourceManager resourceManager, Stream<Identifier> idStream, Profiler profiler, int mipmapLevel, CallbackInfoReturnable<SpriteAtlasTexture.Data> cir, Set<Identifier> set, int i, TextureStitcher textureStitcher, /* ... */ int maxLevel, List<Sprite> sprites) {
		SpriteAtlasTexture atlas = (SpriteAtlasTexture) (Object) this;
		DirTnt.LOGGER.debug("stitch {}", atlas.getId());

		List<SpriteRecipe> recipes = API.recipeMap.get(getId());
		if (recipes == null) return;

		for (var recipe : recipes) {
			// simulate loadSprites() for our new sprites
			textureStitcher.getStitchedSprites((info, atlasWidth, atlasHeight, x, y) -> {
				if (info.getId().equals(recipe.getSpriteId())) {
					DirTnt.LOGGER.debug("getStitchedSprites() {} {} {} {} {} {} {}", recipe.getSpriteId(), atlas, maxLevel, atlasWidth, atlasHeight, x, y);
					sprites.add(recipe.generateSprite(resourceManager, atlas, maxLevel, atlasWidth, atlasHeight, x, y));
				}
			});
		}
	}
}
