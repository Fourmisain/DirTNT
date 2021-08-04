package fourmisain.dirtnt.mixin;

import fourmisain.dirtnt.entity.DirtTntEntity;
import fourmisain.dirtnt.Dirtable;
import net.minecraft.entity.TntEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Allow TNT entities to be dirty */
@Mixin(TntEntity.class)
public abstract class TntEntityMixin implements Dirtable {
	@Unique
	private boolean isDirty = false;

	public void makeDirty() {
		isDirty = true;
	}

	public boolean isDirty() {
		return isDirty;
	}

	@Inject(method = "explode", at = @At("HEAD"), cancellable = true)
	private void dirtyExplode(CallbackInfo ci) {
		TntEntity self = (TntEntity) (Object) this;

		if (isDirty) {
			DirtTntEntity.createDirtExplosion(self, self.world, self.getBlockPos());
			ci.cancel();
		}
	}
}
