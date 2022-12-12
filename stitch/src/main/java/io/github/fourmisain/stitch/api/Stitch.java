package io.github.fourmisain.stitch.api;

import io.github.fourmisain.stitch.impl.AnimationDataAccess;
import io.github.fourmisain.stitch.impl.StitchImpl;
import io.github.fourmisain.stitch.mixin.SpriteContentsAccessor;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;

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

	public static AnimationResourceMetadata getAnimationData(@NotNull SpriteContents sprite) {
		return ((AnimationDataAccess) sprite).getAnimationData();
	}

	public static Identifier getTextureResourcePath(Identifier id) {
		return new Identifier(id.getNamespace(), String.format("textures/%s%s", id.getPath(), ".png"));
	}
}
