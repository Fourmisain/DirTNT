package fourmisain.dirtnt.client;

import fourmisain.dirtnt.DirTnt;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class DirTNTClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(DirTnt.DIRT_TNT_ENTITY_TYPE, DirtTntEntityRenderer::new);
	}
}
