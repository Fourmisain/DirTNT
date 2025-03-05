package fourmisain.dirtnt.block;

import fourmisain.dirtnt.Dirtable;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.TntBlock;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class DirtTntBlock extends TntBlock {
	public static AbstractBlock.Settings getDefaultSettings() {
		return AbstractBlock.Settings.create()
			.mapColor(MapColor.BRIGHT_RED)
			.breakInstantly()
			.sounds(BlockSoundGroup.GRASS)
			.burnable()
			.solidBlock(Blocks::never);
	}

	public DirtTntBlock(AbstractBlock.Settings settings, Identifier dirtType) {
		super(settings);
		((Dirtable) this).makeDirty(dirtType);
	}
}
