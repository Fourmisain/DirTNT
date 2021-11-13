package fourmisain.dirtnt;

import fourmisain.dirtnt.client.DirtTntEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.block.Block;

import static fourmisain.dirtnt.DirTnt.DIRT_TYPES;

@Environment(EnvType.CLIENT)
public class DirTntClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		for (Block dirtType : DIRT_TYPES) {
			EntityRendererRegistry.register(DirTnt.ENTITY_TYPE_MAP.get(dirtType), (context) -> new DirtTntEntityRenderer(dirtType, context));
		}
	}
}
