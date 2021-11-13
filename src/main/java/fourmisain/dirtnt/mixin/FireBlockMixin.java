package fourmisain.dirtnt.mixin;

import fourmisain.dirtnt.DirTnt;
import fourmisain.dirtnt.Dirtable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Random;

@Mixin(FireBlock.class)
public abstract class FireBlockMixin {
	@Inject(method = "trySpreadingFire",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/block/TntBlock;primeTnt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"
			),
			locals = LocalCapture.CAPTURE_FAILHARD)
	private void enableBurningTntDirtOverride(World world, BlockPos pos, int spreadFactor, Random rand, int currentAge, CallbackInfo ci, int i, BlockState state, Block block) {
		if (((Dirtable) block).isDirty()) {
			DirTnt.dirtyOverride = true;
		}
	}

	@Inject(method = "trySpreadingFire",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/block/TntBlock;primeTnt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V",
					shift = At.Shift.AFTER
			))
	private void disableBurningTntDirtOverride(World world, BlockPos pos, int spreadFactor, Random rand, int currentAge, CallbackInfo ci) {
		DirTnt.dirtyOverride = false;
	}
}
