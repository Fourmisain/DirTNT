package fourmisain.dirtnt.block;

import fourmisain.dirtnt.Dirtable;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Material;
import net.minecraft.block.TntBlock;
import net.minecraft.sound.BlockSoundGroup;

// Not really necessary to have but might as well
public class DirtTntBlock extends TntBlock {
	public DirtTntBlock() {
		super(FabricBlockSettings.of(Material.TNT).breakInstantly().sounds(BlockSoundGroup.GRASS));
		((Dirtable) this).makeDirty();
	}
}
