package io.github.fourmisain.stitch.impl;

import io.github.fourmisain.stitch.api.SpriteRecipe;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/*
 * 1.21.11 added TextureResourceMetadata to SpriteContents
 *
 * Note that this version of Stitch currently does not deal with recursive dependencies, it only does a single generation pass.
 */
public class StitchImpl {
	public static final String MOD_ID = "stitch";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	/** atlas id -> sprite id -> recipe */
	public static final Map<Identifier, Map<Identifier, SpriteRecipe>> atlasRecipes = new LinkedHashMap<>();

	public static final Map<SpriteContents, Optional<AnimationMetadataSection>> animationResources = new ConcurrentHashMap<>();
	public static final Map<SpriteContents, Optional<TextureMetadataSection>> textureResources = new ConcurrentHashMap<>();

	public record SpritesStage(List<SpriteContents> current, List<SpriteSource.Loader> generators) {}

	public static SpritesStage prepareGenerating(List<SpriteContents> sprites, Identifier atlasId, ResourceManager resourceManager) {
		Map<Identifier, SpriteRecipe> recipeMap = StitchImpl.atlasRecipes.getOrDefault(atlasId, Map.of());

		// distribute sprite data
		for (SpriteContents sprite : sprites) {
			for (SpriteRecipe recipe : recipeMap.values()) {
				if (recipe.getDependencies().contains(sprite.name())) {
					recipe.collectSprite(sprite);
				}
			}
		}

		List<SpriteSource.Loader> generators = new ArrayList<>();

		for (var entry : recipeMap.entrySet()) {
			Identifier id = entry.getKey();
			SpriteRecipe recipe = entry.getValue();

			generators.add(spriteOpener -> {
				// actually generate the sprite
				var size = recipe.generateSize();
				var animationMetadata = recipe.generateAnimationResourceMetadata();
				var textureMetadata = recipe.generateTextureResourceMetadata();
				var additionalMetadata = recipe.generateAdditionalMetadata();
				var image = recipe.generateImage(resourceManager);

				if (image == null) return null; // turn into missing texture

				return new SpriteContents(id, size, image, animationMetadata, additionalMetadata, textureMetadata);
			});
		}

		return new SpritesStage(sprites, generators);
	}
}
