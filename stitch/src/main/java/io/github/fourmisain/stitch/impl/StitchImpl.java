package io.github.fourmisain.stitch.impl;

import io.github.fourmisain.stitch.api.SpriteRecipe;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteOpener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/*
 * 1.21.9 slightly changed SpriteContents
 *
 * Note that this version of Stitch currently does not deal with recursive dependencies, it only does a single generation pass.
 */
public class StitchImpl {
	public static final String MOD_ID = "stitch";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	/** atlas id -> sprite id -> recipe */
	public static final Map<Identifier, Map<Identifier, SpriteRecipe>> atlasRecipes = new LinkedHashMap<>();

	public static final Map<SpriteContents, Optional<AnimationResourceMetadata>> animationResources = new ConcurrentHashMap<>();

	public record SpritesStage(List<SpriteContents> current, List<Function<SpriteOpener, SpriteContents>> generators) {}

	public static SpritesStage prepareGenerating(List<SpriteContents> sprites, Identifier atlasId, ResourceManager resourceManager) {
		Map<Identifier, SpriteRecipe> recipeMap = StitchImpl.atlasRecipes.getOrDefault(atlasId, Map.of());

		// distribute sprite data
		for (SpriteContents sprite : sprites) {
			for (SpriteRecipe recipe : recipeMap.values()) {
				if (recipe.getDependencies().contains(sprite.getId())) {
					recipe.collectSprite(sprite);
				}
			}
		}

		List<Function<SpriteOpener, SpriteContents>> generators = new ArrayList<>();

		for (var entry : recipeMap.entrySet()) {
			Identifier id = entry.getKey();
			SpriteRecipe recipe = entry.getValue();

			generators.add(spriteOpener -> {
				// actually generate the sprite
				var size = recipe.generateSize();
				var animationMetadata = recipe.generateAnimationResourceMetadata();
				var additionalMetadata = recipe.generateAdditionalMetadata();
				var image = recipe.generateImage(resourceManager);

				if (image == null) return null; // turn into missing texture

				return new SpriteContents(id, size, image, animationMetadata, additionalMetadata);
			});
		}

		return new SpritesStage(sprites, generators);
	}
}
