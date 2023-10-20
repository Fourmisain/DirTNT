package fourmisain.dirtnt;

import fourmisain.dirtnt.client.DirtTntEntityRenderer;
import fourmisain.dirtnt.client.DirtTntSpriteRecipe;
import io.github.fourmisain.stitch.api.Stitch;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.DelegatingUnbakedModel;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.block.TntBlock;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;

import static fourmisain.dirtnt.DirTnt.BLOCK_MAP;
import static fourmisain.dirtnt.DirTnt.DIRT_TYPES;

@Environment(EnvType.CLIENT)
public class DirTntClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLoadingPlugin.register(pluginContext -> {
			// add block models
			for (var dirtType : DIRT_TYPES) {
				Identifier blockId = DirTnt.getDirtTntBlockId(dirtType);
				pluginContext.addModels(blockId.withPath(p -> "block/" + p));
			}

			pluginContext.resolveModel().register(context -> {
				Identifier id = context.id();

				if (id.getNamespace().equals(DirTnt.MOD_ID)) {
					String path = id.getPath();

					if (path.startsWith("block/")) {
						// set block model
						Identifier blockId = id.withPath(p -> p.substring(6));
						return JsonUnbakedModel.deserialize(getCubeBottomTopBlockModelJson(blockId));
					} else if (path.startsWith("item/")) {
						// for some reason we need to manually set the block item model too
						Identifier blockModelId = id.withPath(p -> "block/" + p.substring(5));
						return new DelegatingUnbakedModel(blockModelId);
					}
				}

				return null;
			});

			// delegate all block state models to our one block model
			for (var entry : BLOCK_MAP.entrySet()) {
				Identifier blockModelId = DirTnt.getDirtTntBlockId(entry.getKey()).withPath(p -> "block/" + p);
				DelegatingUnbakedModel model = new DelegatingUnbakedModel(blockModelId);

				pluginContext.registerBlockStateResolver(entry.getValue(), blockContext -> {
					blockContext.setModel(blockContext.block().getDefaultState(), model);
					blockContext.setModel(blockContext.block().getDefaultState().with(TntBlock.UNSTABLE, true), model);
				});
			}
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
