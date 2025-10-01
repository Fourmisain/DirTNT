package io.github.fourmisain.stitch.api;

import io.github.fourmisain.stitch.impl.StitchImpl;
import io.github.fourmisain.stitch.mixin.MissingSpriteAccessor;
import io.github.fourmisain.stitch.mixin.SpriteContentsAccessor;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public class Stitch {
	private Stitch() {}

	public static void registerRecipe(SpriteRecipe recipe) {
		StitchImpl.atlasRecipes.compute(recipe.getAtlasId(), (atlasId, recipes) -> {
			if (recipes == null) recipes = new LinkedHashMap<>();

			recipes.compute(recipe.getSpriteId(), (spriteId, r) -> {
				if (r != null) throw new IllegalArgumentException("duplicate sprite recipe id: " + spriteId);

				return recipe;
			});

			return recipes;
		});
	}

	public static NativeImage getImage(@NotNull SpriteContents sprite) {
		return ((SpriteContentsAccessor) sprite).getImage();
	}

	/** note: returns null after stitching process is done */
	public static Optional<AnimationResourceMetadata> getAnimationResourceMetadata(@NotNull SpriteContents sprite) {
		return StitchImpl.animationResources.get(sprite);
	}

	public static List<ResourceMetadataSerializer.Value<?>> getAdditionalMetadata(@NotNull SpriteContents sprite) {
		return ((SpriteContentsAccessor) sprite).getAdditionalMetadata();
	}

	public static Identifier getTextureResourcePath(Identifier id) {
		return Identifier.of(id.getNamespace(), String.format("textures/%s%s", id.getPath(), ".png"));
	}

	public static NativeImage getMissingSprite(int width, int height) {
		return MissingSpriteAccessor.invokeCreateImage(width, height);
	}

	/** blend color2 onto image */
	public static void blendColors(NativeImage image, int x, int y, int color2) {
		int color1 = image.getColorArgb(x, y);
		image.setColorArgb(x, y, Stitch.blendColors(color1, color2));
	}

	/** blend color2 onto color1 */
	public static int blendColors(int color1, int color2) {
		float a1 = ColorHelper.getAlpha(color1) / 255f;
		float r1 = ColorHelper.getRed(color1)   / 255f;
		float g1 = ColorHelper.getGreen(color1) / 255f;
		float b1 = ColorHelper.getBlue(color1)  / 255f;

		float a2 = ColorHelper.getAlpha(color2) / 255f;
		float r2 = ColorHelper.getRed(color2)   / 255f;
		float g2 = ColorHelper.getGreen(color2) / 255f;
		float b2 = ColorHelper.getBlue(color2)  / 255f;

		// if a2 is 1, take color2, if it is 0, take color1
		float a3 = a2 * a2 + (1 - a2) * a1;
		float r3 = a2 * r2 + (1 - a2) * r1;
		float g3 = a2 * g2 + (1 - a2) * g1;
		float b3 = a2 * b2 + (1 - a2) * b1;

		// unsure if clamping is needed, better safe than sorry
		return ColorHelper.getArgb(
			(int) Math.clamp(a3 * 255f, 0, 255f),
			(int) Math.clamp(r3 * 255f, 0, 255f),
			(int) Math.clamp(g3 * 255f, 0, 255f),
			(int) Math.clamp(b3 * 255f, 0, 255f)
		);
	}
}
