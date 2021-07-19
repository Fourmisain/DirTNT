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
	boolean isDirtTnt = false;

	public void makeDirty() {
		isDirtTnt = true;
	}

	@Inject(method = "explode", at = @At("HEAD"), cancellable = true)
	private void explode(CallbackInfo ci) {
		TntEntity self = (TntEntity) (Object) this;

		if (isDirtTnt) {
			DirtTntEntity.createDirtExplosion(self, self.world, self.getBlockPos());
			ci.cancel();
		}
	}
}
