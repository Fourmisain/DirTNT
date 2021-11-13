package fourmisain.dirtnt;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public interface Dirtable {
	void makeDirty(Block type);
	Block getDirtType();

	default boolean isDirty() {
		return getDirtType() != Blocks.AIR;
	}
}
