package fourmisain.dirtnt.mixin;

import fourmisain.dirtnt.Dirtable;
import fourmisain.dirtnt.entity.DirtTntEntity;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
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
	private Block dirtType = Blocks.AIR;

	@Override
	public void makeDirty(Block dirtType) {
		this.dirtType = dirtType;
	}

	@Override
	public Block getDirtType() {
		return dirtType;
	}

	@Inject(method = "explode", at = @At("HEAD"), cancellable = true)
	private void dirtyExplode(CallbackInfo ci) {
		TntEntity self = (TntEntity) (Object) this;

		if (isDirty()) {
			DirtTntEntity.createDirtExplosion(getDirtType(), self, self.world);
			ci.cancel();
		}
	}
}
