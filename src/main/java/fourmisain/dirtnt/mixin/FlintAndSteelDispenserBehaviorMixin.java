package fourmisain.dirtnt.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import fourmisain.dirtnt.DirTnt;
import fourmisain.dirtnt.Dirtable;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Modifies the FLINT_AND_STEEL dispenser behavior to light Dirt TNT
 *
 * This mixin may be replaced with an additional world.getBlockState(pos).getBlock() check inside overridePrimeTnt(),
 * since the FLINT_AND_STEEL DispenserBehavior removes the block *after* priming the TNT.
 * This is a tad more efficient though.
 */
@Mixin(targets = "net.minecraft.block.dispenser.DispenserBehavior$17")
public abstract class FlintAndSteelDispenserBehaviorMixin {
	@Inject(method = "dispenseSilently(Lnet/minecraft/util/math/BlockPointer;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/block/TntBlock;primeTnt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"
			))
	protected void enableDispensedTntDirtOverride(BlockPointer pointer, ItemStack stack, CallbackInfoReturnable<ItemStack> ci,
			@Local BlockState blockState) {
		DirTnt.dirtyOverride = ((Dirtable) blockState.getBlock()).getDirtType();
	}

	@Inject(method = "dispenseSilently(Lnet/minecraft/util/math/BlockPointer;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/block/TntBlock;primeTnt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V",
					shift = At.Shift.AFTER
			))
	protected void disableDispensedTntDirtOverride(BlockPointer pointer, ItemStack stack, CallbackInfoReturnable<ItemStack> ci) {
		DirTnt.dirtyOverride = null;
	}
}
