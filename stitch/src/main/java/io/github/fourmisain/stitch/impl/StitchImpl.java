package io.github.fourmisain.stitch.impl;

import io.github.fourmisain.stitch.api.SpriteRecipe;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteDimensions;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/*
 * 1.19.3 rewrote texture stiching:
 *
 * The main logic is found in SpriteLoader.method_47661, it is called for each atlas, so for vanilla, 10 times.
 * It is a 3 step async process of:
 *  1. loading a List<Supplier<SpriteContents>> for the given atlas id
 *  2. executing each supplier asynchronously, resulting in a List<SpriteContents>
 *  3. adding the finished SpriteContents to the TextureStitcher and calling stitch()
 *
 * This version of Stitch simply uses an @Overwrite to add some steps inbetween step 2 and 3,
 * see this comment in the previous commit for the full reason why. tl;dr deadlock potential and performance
 *
 * Note that this version of Stitch currently does not deal with recursive depencies, it only does a single generation pass.
 */
public class StitchImpl {
	public static final String MOD_ID = "stitch";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	/** atlas id -> sprite id -> recipe */
	public static final Map<Identifier, Map<Identifier, SpriteRecipe>> atlasRecipes = new LinkedHashMap<>();

	public static final ThreadLocal<Identifier> atlasId = ThreadLocal.withInitial(() -> null);

	public record SpritesStage(List<SpriteContents> current, List<Supplier<SpriteContents>> generators) {}

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

		List<Supplier<SpriteContents>> generators = new ArrayList<>();

		for (var entry : recipeMap.entrySet()) {
			Identifier id = entry.getKey();
			SpriteRecipe recipe = entry.getValue();

			generators.add(() -> {
				// actually generate the sprite
				SpriteDimensions size = recipe.generateSize();
				AnimationResourceMetadata animationData = recipe.generateAnimationData();
				NativeImage image = recipe.generateImage(resourceManager);

				if (image == null) return null; // turn into missing texture

				return new SpriteContents(id, size, image, animationData);
			});
		}

		return new SpritesStage(sprites, generators);
	}
}
