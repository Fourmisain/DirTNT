package fourmisain.dirtnt.mixin;

import fourmisain.dirtnt.DirTnt;
import fourmisain.dirtnt.Dirtable;
import fourmisain.dirtnt.entity.DirtTntEntity;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
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
	private boolean isDirty = false;

	public void makeDirty() {
		isDirty = true;
	}

	public boolean isDirty() {
		return isDirty;
	}

	@Inject(method = {"onBlockAdded", "neighborUpdate", "onBreak", "onProjectileHit"}, at = @At("HEAD"))
	private void enableDirtExplosion(CallbackInfo ci) {
		if (isDirty) DirTnt.dirtyOverride = true;
	}

	@Inject(method = {"onUse"}, at = @At("HEAD"))
	private void enableDirtExplosion(CallbackInfoReturnable<ActionResult> cir) {
		if (isDirty) DirTnt.dirtyOverride = true;
	}

	@Inject(method = {"onBlockAdded", "neighborUpdate", "onBreak", "onProjectileHit"}, at = @At("RETURN"))
	private void disableDirtExplosion(CallbackInfo ci) {
		DirTnt.dirtyOverride = false;
	}

	@Inject(method = {"onUse"}, at = @At("RETURN"))
	private void disableDirtExplosion(CallbackInfoReturnable<ActionResult> cir) {
		DirTnt.dirtyOverride = false;
	}

	@Inject(method = "primeTnt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/LivingEntity;)V", at = @At("HEAD"), cancellable = true)
	private static void primeDirtTnt(World world, BlockPos pos, LivingEntity igniter, CallbackInfo ci) {
		if (DirTnt.dirtyOverride && !world.isClient) {
			DirtTntEntity tntEntity = new DirtTntEntity(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			world.spawnEntity(tntEntity);
			world.playSound(null, tntEntity.getX(), tntEntity.getY(), tntEntity.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
			ci.cancel();
		}
	}

	@Inject(method = "onDestroyedByExplosion", at = @At("HEAD"), cancellable = true)
	public void dirtTntDestroyedByExplosion(World world, BlockPos pos, Explosion explosion, CallbackInfo ci) {
		if (isDirty && !world.isClient) {
			DirtTntEntity tntEntity = new DirtTntEntity(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			tntEntity.setFuse((short)(world.random.nextInt(tntEntity.getFuse() / 4) + tntEntity.getFuse() / 8));
			world.spawnEntity(tntEntity);
			ci.cancel();
		}
	}
}
