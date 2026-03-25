package fourmisain.dirtnt.client;

import fourmisain.dirtnt.Dirtable;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntRenderer;
import net.minecraft.resources.Identifier;

public class DirtTntRenderer extends TntRenderer {
	public DirtTntRenderer(Identifier dirtType, EntityRendererProvider.Context context) {
		super(context);
		((Dirtable) this).makeDirty(dirtType);
	}
}
