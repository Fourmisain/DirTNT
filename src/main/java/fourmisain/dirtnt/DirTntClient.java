package fourmisain.dirtnt;

import fourmisain.dirtnt.client.DirtTntEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class DirTntClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(DirTnt.DIRT_TNT_ENTITY_TYPE, DirtTntEntityRenderer::new);
	}
}
