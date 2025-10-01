package fourmisain.dirtnt;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import fourmisain.dirtnt.client.DirtTntEntityRenderer;
import fourmisain.dirtnt.client.DirtTntSpriteRecipe;
import io.github.fourmisain.stitch.api.Stitch;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.render.entity.EntityRendererFactories;
import net.minecraft.client.render.model.json.BlockModelDefinition;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import static fourmisain.dirtnt.DirTnt.DIRT_TYPES;

public class DirTntClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLoadingPlugin.register(pluginContext -> {
			for (var dirtType : DIRT_TYPES) {
				Identifier blockId = DirTnt.getDirtTntBlockId(dirtType);
				Identifier blockModelId = blockId.withPrefixedPath("block/");

				pluginContext.registerBlockStateResolver(DirTnt.BLOCK_MAP.get(dirtType), context -> {
					JsonObject jsonElement = JsonHelper.deserialize(getBlockStatesJson(blockModelId));
					var modelDefinition = BlockModelDefinition.CODEC.parse(JsonOps.INSTANCE, jsonElement).getOrThrow(JsonParseException::new);

					modelDefinition.simpleModels().ifPresent(modelVariants -> {
						modelVariants.load(context.block().getStateManager(), () -> blockId + "/" + DirTnt.MOD_ID, context::setModel);
					});
				});
			}
		});

		for (Identifier dirtType : DIRT_TYPES) {
			EntityRendererFactories.register(DirTnt.ENTITY_TYPE_MAP.get(dirtType), context -> new DirtTntEntityRenderer(dirtType, context));

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
