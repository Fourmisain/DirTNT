package fourmisain.dirtnt;

import fourmisain.dirtnt.client.DirtTntEntityRenderer;
import fourmisain.dirtnt.client.DirtTntSpriteRecipe;
import io.github.fourmisain.stitch.api.Stitch;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;

import static fourmisain.dirtnt.DirTnt.DIRT_TYPES;

@Environment(EnvType.CLIENT)
public class DirTntClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// TODO use ModelLoadingPlugin
		ModelLoadingRegistry.INSTANCE.registerVariantProvider((manager) -> (modelId, context) -> {
			if (modelId.getNamespace().equals(DirTnt.MOD_ID)) {
				return JsonUnbakedModel.deserialize(getCubeBottomTopBlockModelJson(modelId));
			}

			return null;
		});

		for (Identifier dirtType : DIRT_TYPES) {
			EntityRendererRegistry.register(DirTnt.ENTITY_TYPE_MAP.get(dirtType), (context) -> new DirtTntEntityRenderer(dirtType, context));

			Stitch.registerRecipe(new DirtTntSpriteRecipe(dirtType, "side"));
			Stitch.registerRecipe(new DirtTntSpriteRecipe(dirtType, "top"));
			Stitch.registerRecipe(new DirtTntSpriteRecipe(dirtType, "bottom"));
		}
	}

	public static String getCubeBottomTopBlockModelJson(Identifier modelId) {
		String namespace = modelId.getNamespace();
		String path = modelId.getPath();

		return String.format("""
			{
				"parent": "minecraft:block/cube_bottom_top",
				"textures": {
					"top": "%s:block/%s_top",
					"bottom": "%s:block/%s_bottom",
					"side": "%s:block/%s_side"
				}
			}
			""", namespace, path, namespace, path, namespace, path);
	}
}
