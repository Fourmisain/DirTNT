package fourmisain.dirtnt.client;

import fourmisain.dirtnt.DirTnt;
import fourmisain.dirtnt.api.API;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static fourmisain.dirtnt.DirTnt.DIRT_TYPES;

@Environment(EnvType.CLIENT)
public class DirTntClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {

		ModelLoadingRegistry.INSTANCE.registerVariantProvider((manager) -> (modelId, context) -> {
			if (modelId.getNamespace().equals(DirTnt.MOD_ID)) {
				// get block from modelId
				String path = modelId.getPath();
				if (!path.endsWith("_tnt")) throw new AssertionError();
				String dirtName = path.substring(0, path.length() - "_tnt".length());

				return JsonUnbakedModel.deserialize(getBlockModelJson(dirtName));
			}

			return null;
		});

		for (Block dirtType : DIRT_TYPES) {
			EntityRendererRegistry.register(DirTnt.ENTITY_TYPE_MAP.get(dirtType), (context) -> new DirtTntEntityRenderer(dirtType, context));

			Identifier dirtId = Registry.BLOCK.getId(dirtType);

			API.addRecipe(new DirtTntSpriteRecipe(dirtId, "side"));
			API.addRecipe(new DirtTntSpriteRecipe(dirtId, "top"));
			API.addRecipe(new DirtTntSpriteRecipe(dirtId, "bottom"));
		}
	}

	public static String getBlockModelJson(String dirtName) {
		return String.format("""
			{
				"parent": "minecraft:block/cube_bottom_top",
				"textures": {
					"top": "dirtnt:block/%s_tnt_top",
					"bottom": "dirtnt:block/%s_tnt_bottom",
					"side": "dirtnt:block/%s_tnt_side"
				}
			}
			""", dirtName, dirtName, dirtName);
	}
}
