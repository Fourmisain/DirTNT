package fourmisain.dirtnt.mixin;

import fourmisain.dirtnt.Dirtable;
import fourmisain.dirtnt.entity.PrimedDirtTnt;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.item.PrimedTnt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Allow TNT entities to be dirty */
@Mixin(PrimedTnt.class)
public abstract class PrimedTntMixin implements Dirtable {
	@Unique
	private Identifier dirtType = null;

	@Override
	public void makeDirty(Identifier dirtType) {
		this.dirtType = dirtType;
	}

	@Override
	public Identifier getDirtType() {
		return dirtType;
	}

	@Inject(method = "explode", at = @At("HEAD"), cancellable = true)
	private void dirtyExplode(CallbackInfo ci) {
		PrimedTnt self = (PrimedTnt) (Object) this;

		if (isDirty()) {
			PrimedDirtTnt.createDirtExplosion(getDirtType(), self, self.level(), true);
			ci.cancel();
		}
	}
}
