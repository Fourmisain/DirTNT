package fourmisain.dirtnt.client;

import fourmisain.dirtnt.DirTnt;
import fourmisain.dirtnt.Dirtable;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.TntEntityRenderer;

@SuppressWarnings("ConstantConditions")
@Environment(EnvType.CLIENT)
public class DirTNTClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.INSTANCE.register(DirTnt.DIRT_TNT_ENTITY_TYPE, (context) -> {
            // use a dirty TNT renderer
            Dirtable renderer = (Dirtable) new TntEntityRenderer(context);
            renderer.makeDirty();
            return (TntEntityRenderer) renderer;
        });
    }
}
