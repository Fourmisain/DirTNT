package fourmisain.dirtnt.client;

import fourmisain.dirtnt.Dirtable;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.TntEntityRenderer;

public class DirtTntEntityRenderer extends TntEntityRenderer {
	public DirtTntEntityRenderer(EntityRendererFactory.Context context) {
		super(context);
		((Dirtable) this).makeDirty();
	}
}
