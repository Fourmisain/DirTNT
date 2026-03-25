package fourmisain.dirtnt.mixin;

import fourmisain.dirtnt.DirTnt;
import fourmisain.dirtnt.Dirtable;
import fourmisain.dirtnt.entity.PrimedDirtTnt;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TntBlock;
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

	@Inject(method = {"onPlace", "neighborChanged", "onProjectileHit"}, at = @At("HEAD"))
	private void enableTntDirtOverride(CallbackInfo ci) {
		DirTnt.dirtyOverride = getDirtType();
	}

	@Inject(method = {"playerWillDestroy", "useItemOn"}, at = @At("HEAD"))
	private void enableTntDirtOverride(CallbackInfoReturnable<?> cir) {
		DirTnt.dirtyOverride = getDirtType();
	}

	@Inject(method = {"onPlace", "neighborChanged", "onProjectileHit"}, at = @At("RETURN"))
	private void disableTntDirtOverride(CallbackInfo ci) {
		DirTnt.dirtyOverride = null;
	}

	@Inject(method = {"playerWillDestroy", "useItemOn"}, at = @At("RETURN"))
	private void disableTntDirtOverride(CallbackInfoReturnable<?> cir) {
		DirTnt.dirtyOverride = null;
	}

	@Inject(method = "prime(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
	private static void primeDirtTnt(Level level, BlockPos pos, LivingEntity igniter, CallbackInfoReturnable<Boolean> cir) {
		if (DirTnt.dirtyOverride != null && !level.isClientSide()) {
			PrimedDirtTnt tnt = new PrimedDirtTnt(DirTnt.dirtyOverride, level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			level.addFreshEntity(tnt);
			level.playSound(null, tnt.getX(), tnt.getY(), tnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
			cir.setReturnValue(true);
			cir.cancel();
		}
	}

	@Inject(method = "wasExploded", at = @At("HEAD"), cancellable = true)
	public void dirtTntDestroyedByExplosion(ServerLevel level, BlockPos pos, Explosion explosion, CallbackInfo ci) {
		if (isDirty() && !level.isClientSide()) {
			PrimedDirtTnt tnt = new PrimedDirtTnt(getDirtType(), level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			tnt.setFuse((short)(level.getRandom().nextInt(tnt.getFuse() / 4) + tnt.getFuse() / 8));
			level.addFreshEntity(tnt);
			ci.cancel();
		}
	}
}
