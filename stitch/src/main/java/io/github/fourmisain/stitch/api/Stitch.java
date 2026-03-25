package io.github.fourmisain.stitch.api;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.fourmisain.stitch.impl.StitchImpl;
import io.github.fourmisain.stitch.mixin.MissingSpriteAccessor;
import io.github.fourmisain.stitch.mixin.SpriteContentsAccessor;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.ARGB;
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
		return ((SpriteContentsAccessor) sprite).getOriginalImage();
	}

	/** note: returns null after stitching process is done */
	public static Optional<AnimationMetadataSection> getAnimationResourceMetadata(@NotNull SpriteContents sprite) {
		return StitchImpl.animationResources.get(sprite);
	}

	/** note: returns null after stitching process is done */
	public static Optional<TextureMetadataSection> getTextureResourceMetadata(@NotNull SpriteContents sprite) {
		return StitchImpl.textureResources.get(sprite);
	}

	public static List<MetadataSectionType.WithValue<?>> getAdditionalMetadata(@NotNull SpriteContents sprite) {
		return ((SpriteContentsAccessor) sprite).getAdditionalMetadata();
	}

	public static Identifier getTextureResourcePath(Identifier id) {
		return Identifier.fromNamespaceAndPath(id.getNamespace(), String.format("textures/%s%s", id.getPath(), ".png"));
	}

	public static NativeImage getMissingSprite(int width, int height) {
		return MissingSpriteAccessor.invokeGenerateMissingImage(width, height);
	}

	/** blend color2 onto image */
	public static void blendColors(NativeImage image, int x, int y, int color2) {
		int color1 = image.getPixel(x, y);
		image.setPixel(x, y, Stitch.blendColors(color1, color2));
	}

	/** blend color2 onto color1 */
	public static int blendColors(int color1, int color2) {
		float a1 = ARGB.alpha(color1) / 255f;
		float r1 = ARGB.red(color1)   / 255f;
		float g1 = ARGB.green(color1) / 255f;
		float b1 = ARGB.blue(color1)  / 255f;

		float a2 = ARGB.alpha(color2) / 255f;
		float r2 = ARGB.red(color2)   / 255f;
		float g2 = ARGB.green(color2) / 255f;
		float b2 = ARGB.blue(color2)  / 255f;

		// if a2 is 1, take color2, if it is 0, take color1
		float a3 = a2 * a2 + (1 - a2) * a1;
		float r3 = a2 * r2 + (1 - a2) * r1;
		float g3 = a2 * g2 + (1 - a2) * g1;
		float b3 = a2 * b2 + (1 - a2) * b1;

		// unsure if clamping is needed, better safe than sorry
		return ARGB.color(
			(int) Math.clamp(a3 * 255f, 0, 255f),
			(int) Math.clamp(r3 * 255f, 0, 255f),
			(int) Math.clamp(g3 * 255f, 0, 255f),
			(int) Math.clamp(b3 * 255f, 0, 255f)
		);
	}
}
