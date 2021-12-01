package io.github.fourmisain.stitch.api;

import io.github.fourmisain.stitch.impl.StitchImpl;
import io.github.fourmisain.stitch.mixin.SpriteAccessor;
import io.github.fourmisain.stitch.mixin.SpriteInfoAccessor;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
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

	public static NativeImage getImage(@NotNull Sprite sprite) {
		return ((SpriteAccessor) sprite).getImages()[0];
	}

	public static AnimationResourceMetadata getAnimationData(Sprite.Info spriteInfo) {
		return ((SpriteInfoAccessor) (Object) spriteInfo).getAnimationData();
	}

	public static Identifier getTextureResourcePath(Identifier id) {
		return new Identifier(id.getNamespace(), String.format("textures/%s%s", id.getPath(), ".png"));
	}
}
