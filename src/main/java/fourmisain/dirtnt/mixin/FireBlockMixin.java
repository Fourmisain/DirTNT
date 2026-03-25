package fourmisain.dirtnt.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import fourmisain.dirtnt.DirTnt;
import fourmisain.dirtnt.Dirtable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
public abstract class FireBlockMixin {
	@Inject(
		method = "checkBurnOut",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/TntBlock;prime(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z"
		)
	)
	private void enableBurningTntDirtOverride(Level world, BlockPos pos, int spreadFactor, RandomSource random, int currentAge, CallbackInfo ci,
			@Local Block block) {
		DirTnt.dirtyOverride = ((Dirtable) block).getDirtType();
	}

	@Inject(
		method = "checkBurnOut",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/TntBlock;prime(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z",
			shift = At.Shift.AFTER
		)
	)
	private void disableBurningTntDirtOverride(Level world, BlockPos pos, int spreadFactor, RandomSource random, int currentAge, CallbackInfo ci) {
		DirTnt.dirtyOverride = null;
	}
}
