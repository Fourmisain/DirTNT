package fourmisain.dirtnt.mixin;

import net.minecraft.references.BlockItemId;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Items.class)
public interface ItemsAccessor {
	@Invoker("registerBlock")
	static Item invokeRegisterBlock(BlockItemId id, Block block) {
		throw new AssertionError();
	}
}
