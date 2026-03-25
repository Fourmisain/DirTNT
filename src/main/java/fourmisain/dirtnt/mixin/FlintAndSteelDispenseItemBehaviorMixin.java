package fourmisain.dirtnt.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import fourmisain.dirtnt.DirTnt;
import fourmisain.dirtnt.Dirtable;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Modifies the FLINT_AND_STEEL dispenser behavior to light Dirt TNT
 *
 * This mixin may be replaced with an additional level.getBlockState(pos).getBlock() check inside overridePrimeTnt(),
 * since the FLINT_AND_STEEL DispenserBehavior removes the block *after* priming the TNT.
 * This is a tad more efficient though.
 */
@Mixin(targets = "net.minecraft.core.dispenser.DispenseItemBehavior$5")
public abstract class FlintAndSteelDispenseItemBehaviorMixin {
	@Inject(
		method = "execute",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/TntBlock;prime(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z"
		)
	)
	protected void enableDispensedTntDirtOverride(BlockSource blockSource, ItemStack stack, CallbackInfoReturnable<ItemStack> ci,
			@Local BlockState blockState) {
		DirTnt.dirtyOverride = ((Dirtable) blockState.getBlock()).getDirtType();
	}

	@Inject(
		method = "execute",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/TntBlock;prime(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z",
			shift = At.Shift.AFTER
		)
	)
	protected void disableDispensedTntDirtOverride(BlockSource blockSource, ItemStack stack, CallbackInfoReturnable<ItemStack> ci) {
		DirTnt.dirtyOverride = null;
	}
}
