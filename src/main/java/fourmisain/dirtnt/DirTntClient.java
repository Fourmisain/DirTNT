package fourmisain.dirtnt;

import fourmisain.dirtnt.client.DirtTntEntityRenderer;
import fourmisain.dirtnt.client.DirtTntSpriteRecipe;
import io.github.fourmisain.stitch.api.Stitch;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.TntBlock;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.util.Identifier;

import java.io.StringReader;
import java.util.List;

import static fourmisain.dirtnt.DirTnt.DIRT_TYPES;

public class DirTntClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLoadingPlugin.register(pluginContext -> {
			for (var dirtType : DIRT_TYPES) {
				Identifier blockId = DirTnt.getDirtTntBlockId(dirtType);
				Identifier blockModelId = blockId.withPrefixedPath("block/");

				pluginContext.addModels(blockModelId);

				pluginContext.registerBlockStateResolver(DirTnt.BLOCK_MAP.get(dirtType), context -> {
					ModelVariantMap variantMap = ModelVariantMap.fromJson(new StringReader(getBlockStatesJson(blockModelId)));

					for (var value : List.of(false, true)) {
						BlockState blockState = context.block().getDefaultState().with(TntBlock.UNSTABLE, value);
						context.setModel(blockState, variantMap.getVariant(""));
					}
				});
			}

			pluginContext.modifyModelOnLoad().register((unbakedModel, context) -> {
				Identifier id = context.id();

				if (unbakedModel == null && id.getNamespace().equals(DirTnt.MOD_ID)) {
					return JsonUnbakedModel.deserialize(new StringReader(getCubeBottomTopBlockModelJson(id)));
				}

				return unbakedModel;
			});
		});

		for (Identifier dirtType : DIRT_TYPES) {
			EntityRendererRegistry.register(DirTnt.ENTITY_TYPE_MAP.get(dirtType), (context) -> new DirtTntEntityRenderer(dirtType, context));

			Stitch.registerRecipe(new DirtTntSpriteRecipe(dirtType, "side"));
			Stitch.registerRecipe(new DirtTntSpriteRecipe(dirtType, "top"));
			Stitch.registerRecipe(new DirtTntSpriteRecipe(dirtType, "bottom"));
		}
	}

	public static String getCubeBottomTopBlockModelJson(Identifier modelId) {
		return """
			{
				"parent": "minecraft:block/cube_bottom_top",
				"textures": {
					"top": "%s_top",
					"bottom": "%s_bottom",
					"side": "%s_side"
				}
			}
			""".formatted(modelId, modelId, modelId);
	}

	public static String getBlockStatesJson(Identifier modelId) {
		return """
			{
			  "variants": {
			    "": {
			      "model": "%s"
			    }
			  }
			}
			""".formatted(modelId);
	}

	public static String getItemsJson(Identifier modelId) {
		return """
			{
			  "model": {
			    "type": "minecraft:model",
			    "model": "%s"
			  }
			}
			""".formatted(modelId);
	}
}
