package fourmisain.dirtnt.block;

import fourmisain.dirtnt.Dirtable;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.TntBlock;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class DirtTntBlock extends TntBlock {
	public DirtTntBlock(Identifier dirtType) {
		super(AbstractBlock.Settings.create()
			.mapColor(MapColor.BRIGHT_RED)
			.breakInstantly()
			.sounds(BlockSoundGroup.GRASS)
			.burnable()
			.solidBlock((blockState, blockView, blockPos) -> false));
		((Dirtable) this).makeDirty(dirtType);
	}
}
