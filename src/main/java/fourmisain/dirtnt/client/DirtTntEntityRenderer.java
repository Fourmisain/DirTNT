package fourmisain.dirtnt.client;

import fourmisain.dirtnt.Dirtable;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.TntEntityRenderer;
import net.minecraft.util.Identifier;

public class DirtTntEntityRenderer extends TntEntityRenderer {
	public DirtTntEntityRenderer(Identifier dirtType, EntityRendererFactory.Context context) {
		super(context);
		((Dirtable) this).makeDirty(dirtType);
	}
}
