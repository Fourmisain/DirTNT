package fourmisain.dirtnt.mixin;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import fourmisain.dirtnt.DirTnt;
import net.minecraft.data.server.RecipeProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static fourmisain.dirtnt.DirTnt.DIRT_TYPES;
import static fourmisain.dirtnt.DirTnt.getDirtTntBlockId;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
	@Shadow
	private Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes;
	@Shadow
	private Map<Identifier, Recipe<?>> recipesById;

	@Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At("TAIL"))
	protected void dirtnt$addRecipes(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {
		Map<RecipeType<?>, ImmutableMap.Builder<Identifier, Recipe<?>>> newRecipes = new HashMap<>();
		ImmutableMap.Builder<Identifier, Recipe<?>> builder = ImmutableMap.builder();

		/* rebuild recipes */
		for (var entry : recipes.entrySet()) {
			RecipeType<?> type = entry.getKey();
			Map<Identifier, Recipe<?>> recipesByType = entry.getValue();
			var builderByType = newRecipes.computeIfAbsent(type, (recipeType) -> ImmutableMap.builder());

			for (var recipeEntry : recipesByType.entrySet()) {
				Identifier recipeId = recipeEntry.getKey();
				Recipe<?> recipe = recipeEntry.getValue();

				builderByType.put(recipeId, recipe);
				builder.put(recipeId, recipe);
			}
		}

		// add new recipes
		DirTnt.LOGGER.debug("auto-gen dirt tnt recipes");
		for (Identifier dirtType : DIRT_TYPES) {
			Identifier id = getDirtTntBlockId(dirtType);
			Optional<Item> optionalItem = Registry.ITEM.getOrEmpty(dirtType);

			// not every block has an associated item (and air is not a valid crafting ingredient)
			if (optionalItem.isEmpty() || optionalItem.get() == Items.AIR) {
				DirTnt.LOGGER.warn("can't auto-gen recipe for dirt type {}", dirtType);
				continue;
			}

			ShapedRecipeJsonBuilder.create(DirTnt.ITEM_MAP.get(dirtType))
					.input('#', optionalItem.get())
					.input('X', Items.TNT)
					.pattern("###")
					.pattern("#X#")
					.pattern("###")
					.criterion("has_gunpowder", RecipeProvider.conditionsFromItem(Items.GUNPOWDER))
					.offerTo(exporter -> {
						ShapedRecipe recipe = new ShapedRecipe.Serializer().read(id, exporter.toJson());

						// add recipe
						var builderByType = newRecipes.get(recipe.getType());
						builderByType.put(recipe.getId(), recipe);
						builder.put(recipe.getId(), recipe);
					}, id);
		}

		this.recipes = newRecipes.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, (entry) -> entry.getValue().build()));
		this.recipesById = builder.build();
	}
}
