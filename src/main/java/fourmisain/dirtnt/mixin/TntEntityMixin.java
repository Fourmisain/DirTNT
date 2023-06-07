package fourmisain.dirtnt.mixin;

import fourmisain.dirtnt.Dirtable;
import fourmisain.dirtnt.entity.DirtTntEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Allow TNT entities to be dirty */
@Mixin(TntEntity.class)
public abstract class TntEntityMixin implements Dirtable {
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
		TntEntity self = (TntEntity) (Object) this;

		if (isDirty()) {
			DirtTntEntity.createDirtExplosion(getDirtType(), self, self.getWorld());
			ci.cancel();
		}
	}
}
