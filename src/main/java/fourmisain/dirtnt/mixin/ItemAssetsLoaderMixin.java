package fourmisain.dirtnt.mixin;

import com.google.gson.JsonParser;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.JsonOps;
import fourmisain.dirtnt.DirTnt;
import fourmisain.dirtnt.DirTntClient;
import net.minecraft.client.item.ItemAsset;
import net.minecraft.client.item.ItemAssetsLoader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static fourmisain.dirtnt.DirTnt.DIRT_TYPES;

@Mixin(ItemAssetsLoader.class)
public abstract class ItemAssetsLoaderMixin {
	@Shadow @Final
	private static ResourceFinder FINDER;

	@ModifyArg(
		method = "method_65932", // load forEach supplyAsync lambda
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/Util;combineSafe(Ljava/util/List;)Ljava/util/concurrent/CompletableFuture;"
		)
	)
	private static List<CompletableFuture<ItemAssetsLoader.Definition>> addItemModels(List<CompletableFuture<ItemAssetsLoader.Definition>> models,
			@Local(argsOnly = true) Map<Identifier, Resource> resources, @Local(argsOnly = true) Executor executor) {
		for (var dirtType : DIRT_TYPES) {
			Identifier id = DirTnt.getDirtTntBlockId(dirtType);
			Identifier blockModelId = id.withPrefixedPath("block/");

			if (resources.containsKey(FINDER.toResourcePath(id)))
				continue;

			models.add(CompletableFuture.supplyAsync(() -> {
				var itemAsset = ItemAsset.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(new StringReader(DirTntClient.getItemsJson(blockModelId))))
					.ifError((error) -> DirTnt.LOGGER.error("Couldn't parse item model '{}': {}", id, error.message()))
					.result().orElse(null);

				return new ItemAssetsLoader.Definition(id, itemAsset);
			}, executor));
		}

		return models;
	}
}
