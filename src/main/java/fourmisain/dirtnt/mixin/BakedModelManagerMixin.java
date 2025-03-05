package fourmisain.dirtnt.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import fourmisain.dirtnt.DirTnt;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.io.StringReader;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static fourmisain.dirtnt.DirTnt.DIRT_TYPES;

@Mixin(BakedModelManager.class)
public abstract class BakedModelManagerMixin {
	@ModifyArg(
		method = "method_45899", // reloadModels' thenCompose lambda
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/Util;combineSafe(Ljava/util/List;)Ljava/util/concurrent/CompletableFuture;"
		)
	)
	private static List<CompletableFuture<Pair<Identifier, JsonUnbakedModel>>> addUnbakedModels(List<CompletableFuture<Pair<Identifier, JsonUnbakedModel>>> models,
			@Local(argsOnly = true) Executor executor) {
		// replaced through Fabric API later
		JsonUnbakedModel dummy = JsonUnbakedModel.deserialize(new StringReader("{\"parent\": \"minecraft:block/tnt\"}"));

		for (var dirtType : DIRT_TYPES) {
			Identifier blockId = DirTnt.getDirtTntBlockId(dirtType);
			models.add(CompletableFuture.completedFuture(Pair.of(blockId.withPath(p -> "item/" + p), dummy)));
		}

		return models;
	}
}
