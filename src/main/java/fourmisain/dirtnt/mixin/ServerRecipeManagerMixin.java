package fourmisain.dirtnt.mixin;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.JsonOps;
import fourmisain.dirtnt.DirTnt;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.PreparedRecipes;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Optional;
import java.util.SortedMap;

import static fourmisain.dirtnt.DirTnt.DIRT_TYPES;
import static fourmisain.dirtnt.DirTnt.getDirtTntBlockId;

@Mixin(ServerRecipeManager.class)
public abstract class ServerRecipeManagerMixin {
	@Shadow @Final
	private RegistryWrapper.WrapperLookup registries;

	@Inject(
		method = "prepare(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)Lnet/minecraft/recipe/PreparedRecipes;",
		at = @At(value = "NEW", target = "(I)Ljava/util/ArrayList;")
	)
	public void addTntRecipes(ResourceManager resourceManager, Profiler profiler, CallbackInfoReturnable<PreparedRecipes> cir,
			@Local SortedMap<Identifier, Recipe<?>> recipes) {
		for (Identifier dirtType : DIRT_TYPES) {
			Optional<Item> dirt = Registries.ITEM.getOptionalValue(dirtType);
			if (dirt.isEmpty() || dirt.get() == Items.AIR) { // not every block has an associated item (and air is not a valid crafting ingredient)
				DirTnt.LOGGER.warn("can't auto-gen recipe for dirt type {}", dirtType);
				continue;
			}

			Identifier id = getDirtTntBlockId(dirtType);

			try (Reader reader = new StringReader(DirTnt.getRecipeJson(dirtType, id))) {
				Recipe.CODEC.parse(registries.getOps(JsonOps.INSTANCE), JsonParser.parseReader(reader)).ifSuccess(value -> {
					recipes.putIfAbsent(id, value); // ignore if it exists already
				}).ifError(error -> DirTnt.LOGGER.error("Couldn't parse recipe '{}': {}", id, error));
			} catch (IllegalArgumentException | IOException | JsonParseException e) {
				DirTnt.LOGGER.error("Couldn't parse recipe '{}'", id, e);
			}
		}
	}
}
