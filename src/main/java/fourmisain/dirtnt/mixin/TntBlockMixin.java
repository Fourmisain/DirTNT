package fourmisain.dirtnt.mixin;

import fourmisain.dirtnt.Dirtable;
import fourmisain.dirtnt.entity.DirtTntEntity;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Allow TNT blocks to be dirty
 * This is currently not very compatible, but it is very short and extensible.
 */
@Mixin(TntBlock.class)
public abstract class TntBlockMixin implements Dirtable {
	@Unique
	boolean isDirtTnt = false;

	public void makeDirty() {
		isDirtTnt = true;
	}

	@Unique
	private static void primeDirtTnt(World world, BlockPos pos) {
		if (!world.isClient) {
			DirtTntEntity tntEntity = new DirtTntEntity(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			world.spawnEntity(tntEntity);
			world.playSound(null, tntEntity.getX(), tntEntity.getY(), tntEntity.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
		}
	}

	@Shadow
	private static void primeTnt(World world, BlockPos pos, @Nullable LivingEntity igniter) {}

	@Redirect(method = {"onBlockAdded", "neighborUpdate", "onBreak"},
		at = @At(value = "INVOKE", target = "Lnet/minecraft/block/TntBlock;primeTnt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"))
	private void redirectToDirtTnt(World world, BlockPos pos) {
		if (isDirtTnt) {
			primeDirtTnt(world, pos);
		} else {
			TntBlock.primeTnt(world, pos);
		}
	}

	@Redirect(method = {"onUse", "onProjectileHit"},
		at = @At(value = "INVOKE", target = "Lnet/minecraft/block/TntBlock;primeTnt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/LivingEntity;)V"))
	private void redirectToDirtTnt2(World world, BlockPos pos, LivingEntity igniter) {
		if (isDirtTnt) {
			primeDirtTnt(world, pos);
		} else {
			TntBlockMixin.primeTnt(world, pos, igniter);
		}
	}

	@Inject(method = "onDestroyedByExplosion", at = @At("HEAD"), cancellable = true)
	public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion, CallbackInfo ci) {
		if (isDirtTnt && !world.isClient) {
			DirtTntEntity tntEntity = new DirtTntEntity(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			tntEntity.setFuse((short)(world.random.nextInt(tntEntity.getFuseTimer() / 4) + tntEntity.getFuseTimer() / 8));
			world.spawnEntity(tntEntity);
			ci.cancel();
		}
	}
}
