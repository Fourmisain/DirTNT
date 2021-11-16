package fourmisain.dirtnt.mixin;

import fourmisain.dirtnt.DirTnt;
import fourmisain.dirtnt.Dirtable;
import fourmisain.dirtnt.entity.DirtTntEntity;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Allow TNT blocks to be dirty */
@Mixin(TntBlock.class)
public abstract class TntBlockMixin implements Dirtable {
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

	@Inject(method = {"onBlockAdded", "neighborUpdate", "onBreak", "onProjectileHit"}, at = @At("HEAD"))
	private void enableTntDirtOverride(CallbackInfo ci) {
		DirTnt.dirtyOverride = getDirtType();
	}

	@Inject(method = {"onUse"}, at = @At("HEAD"))
	private void enableTntDirtOverride(CallbackInfoReturnable<ActionResult> cir) {
		DirTnt.dirtyOverride = getDirtType();
	}

	@Inject(method = {"onBlockAdded", "neighborUpdate", "onBreak", "onProjectileHit"}, at = @At("RETURN"))
	private void disableTntDirtOverride(CallbackInfo ci) {
		DirTnt.dirtyOverride = null;
	}

	@Inject(method = {"onUse"}, at = @At("RETURN"))
	private void disableTntDirtOverride(CallbackInfoReturnable<ActionResult> cir) {
		DirTnt.dirtyOverride = null;
	}

	@Inject(method = "primeTnt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/LivingEntity;)V", at = @At("HEAD"), cancellable = true)
	private static void primeDirtTnt(World world, BlockPos pos, LivingEntity igniter, CallbackInfo ci) {
		if (DirTnt.dirtyOverride != null && !world.isClient) {
			DirtTntEntity tntEntity = new DirtTntEntity(DirTnt.dirtyOverride, world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			world.spawnEntity(tntEntity);
			world.playSound(null, tntEntity.getX(), tntEntity.getY(), tntEntity.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
			ci.cancel();
		}
	}

	@Inject(method = "onDestroyedByExplosion", at = @At("HEAD"), cancellable = true)
	public void dirtTntDestroyedByExplosion(World world, BlockPos pos, Explosion explosion, CallbackInfo ci) {
		if (isDirty() && !world.isClient) {
			DirtTntEntity tntEntity = new DirtTntEntity(getDirtType(), world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			tntEntity.setFuse((short)(world.random.nextInt(tntEntity.getFuse() / 4) + tntEntity.getFuse() / 8));
			world.spawnEntity(tntEntity);
			ci.cancel();
		}
	}
}
