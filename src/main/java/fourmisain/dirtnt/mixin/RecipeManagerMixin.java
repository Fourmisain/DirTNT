package fourmisain.dirtnt.mixin;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.JsonOps;
import fourmisain.dirtnt.DirTnt;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
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

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
	@Shadow @Final
	private HolderLookup.Provider registries;

	@Inject(
		method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Lnet/minecraft/world/item/crafting/RecipeMap;",
		at = @At(value = "NEW", target = "(I)Ljava/util/ArrayList;")
	)
	public void addTntRecipes(ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfoReturnable<RecipeMap> cir,
			@Local SortedMap<Identifier, Recipe<?>> recipes) {
		for (Identifier dirtType : DIRT_TYPES) {
			Optional<Item> dirt = BuiltInRegistries.ITEM.getOptional(dirtType);
			if (dirt.isEmpty() || dirt.get() == Items.AIR) { // not every block has an associated item (and air is not a valid crafting ingredient)
				DirTnt.LOGGER.warn("can't auto-gen recipe for dirt type {}", dirtType);
				continue;
			}

			Identifier id = getDirtTntBlockId(dirtType);

			try (Reader reader = new StringReader(DirTnt.getRecipeJson(dirtType, id))) {
				Recipe.CODEC.parse(registries.createSerializationContext(JsonOps.INSTANCE), JsonParser.parseReader(reader)).ifSuccess(value -> {
					recipes.putIfAbsent(id, value); // ignore if it exists already
				}).ifError(error -> DirTnt.LOGGER.error("Couldn't parse recipe '{}': {}", id, error));
			} catch (IllegalArgumentException | IOException | JsonParseException e) {
				DirTnt.LOGGER.error("Couldn't parse recipe '{}'", id, e);
			}
		}
	}
}
