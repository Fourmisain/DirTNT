package fourmisain.dirtnt;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import fourmisain.dirtnt.client.DirtTntRenderer;
import fourmisain.dirtnt.client.DirtTntSpriteRecipe;
import io.github.fourmisain.stitch.api.Stitch;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

import static fourmisain.dirtnt.DirTnt.DIRT_TYPES;

public class DirTntClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLoadingPlugin.register(pluginContext -> {
			for (var dirtType : DIRT_TYPES) {
				Identifier blockId = DirTnt.getDirtTntBlockId(dirtType);
				Identifier blockModelId = blockId.withPrefix("block/");

				pluginContext.registerBlockStateResolver(DirTnt.BLOCK_MAP.get(dirtType), context -> {
					JsonObject jsonElement = GsonHelper.parse(getBlockStatesJson(blockModelId));
					var modelDefinition = BlockModelDefinition.CODEC.parse(JsonOps.INSTANCE, jsonElement).getOrThrow(JsonParseException::new);

					modelDefinition.simpleModels().ifPresent(modelVariants -> {
						modelVariants.instantiate(context.block().getStateDefinition(), () -> blockId + "/" + DirTnt.MOD_ID, context::setModel);
					});
				});
			}
		});

		for (Identifier dirtType : DIRT_TYPES) {
			EntityRenderers.register(DirTnt.ENTITY_TYPE_MAP.get(dirtType), context -> new DirtTntRenderer(dirtType, context));

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
