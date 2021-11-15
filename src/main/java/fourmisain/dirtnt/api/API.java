package fourmisain.dirtnt.api;

import fourmisain.dirtnt.mixin.autogen.SpriteAccessor;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class API {
	public static Map<Identifier, List<SpriteRecipe>> recipeMap = new HashMap<>();

	public static NativeImage getImage(@NotNull Sprite sprite) {
		return ((SpriteAccessor) sprite).getImages()[0];
	}

	public static Identifier getTexturePath(Identifier id) {
		return new Identifier(id.getNamespace(), String.format("textures/%s%s", id.getPath(), ".png"));
	}

	public static void addRecipe(SpriteRecipe recipe) {
		recipeMap.compute(recipe.getAtlasId(), (atlasId, recipes) -> {
			if (recipes == null) recipes = new ArrayList<>();
			recipes.add(recipe);
			return recipes;
		});
	}
}
