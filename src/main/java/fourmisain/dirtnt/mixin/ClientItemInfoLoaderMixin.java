package fourmisain.dirtnt.mixin;

import com.google.gson.JsonParser;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.JsonOps;
import fourmisain.dirtnt.DirTnt;
import fourmisain.dirtnt.DirTntClient;
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
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.resources.model.ClientItemInfoLoader;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

import static fourmisain.dirtnt.DirTnt.DIRT_TYPES;

@Mixin(ClientItemInfoLoader.class)
public abstract class ClientItemInfoLoaderMixin {
	@Shadow @Final
	private static FileToIdConverter LISTER;

	@ModifyArg(
		method = "method_65932", // load forEach supplyAsync lambda
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/Util;sequence(Ljava/util/List;)Ljava/util/concurrent/CompletableFuture;"
		)
	)
	private static List<CompletableFuture<ClientItemInfoLoader.PendingLoad>> addItemModels(List<CompletableFuture<ClientItemInfoLoader.PendingLoad>> models,
			@Local(argsOnly = true) Map<Identifier, Resource> resources, @Local(argsOnly = true) Executor executor) {
		for (var dirtType : DIRT_TYPES) {
			Identifier id = DirTnt.getDirtTntBlockId(dirtType);
			Identifier blockModelId = id.withPrefix("block/");

			if (resources.containsKey(LISTER.idToFile(id)))
				continue;

			models.add(CompletableFuture.supplyAsync(() -> {
				var itemAsset = ClientItem.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(new StringReader(DirTntClient.getItemsJson(blockModelId))))
					.ifError((error) -> DirTnt.LOGGER.error("Couldn't parse item model '{}': {}", id, error.message()))
					.result().orElse(null);

				return new ClientItemInfoLoader.PendingLoad(id, itemAsset);
			}, executor));
		}

		return models;
	}
}
