package fourmisain.dirtnt.mixin;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import fourmisain.dirtnt.DirTnt;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static fourmisain.dirtnt.DirTnt.DIRT_TYPES;
import static fourmisain.dirtnt.DirTntClient.getCubeBottomTopBlockModelJson;

// replacement for pluginContext.addModels() + pluginContext.modifyModelOnLoad()
@Mixin(ModelManager.class)
public abstract class ModelManagerMixin {
	@ModifyExpressionValue(
		method = "loadBlockModels",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/concurrent/CompletableFuture;thenCompose(Ljava/util/function/Function;)Ljava/util/concurrent/CompletableFuture;"
		)
	)
	private static CompletableFuture<Map<Identifier, UnbakedModel>> addDirTntModels(CompletableFuture<Map<Identifier, UnbakedModel>> original) {
		return original.thenApply(unbakedModels -> {
			try {
				var builder = ImmutableMap.<Identifier, UnbakedModel>builder()
					.putAll(unbakedModels);

				for (var dirtType : DIRT_TYPES) {
					Identifier blockModelId = DirTnt.getDirtTntBlockId(dirtType).withPrefix("block/");

					var model = CuboidModel.fromStream(new StringReader(getCubeBottomTopBlockModelJson(blockModelId)));

					if (!unbakedModels.containsKey(blockModelId)) {
						builder.put(blockModelId, model);
					}
				}

				return builder.build();
			} catch (Exception e) {
				DirTnt.LOGGER.error("failed to add DirTNT models", e);
				return unbakedModels;
			}
		});
	}
}
