package fourmisain.dirtnt.block;

import fourmisain.dirtnt.Dirtable;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.TntBlock;
import net.minecraft.sound.BlockSoundGroup;

public class DirtTntBlock extends TntBlock {
	public DirtTntBlock(Block dirtType) {
		super(FabricBlockSettings.of(Material.TNT).breakInstantly().sounds(BlockSoundGroup.GRASS));
		((Dirtable) this).makeDirty(dirtType);
	}
}
