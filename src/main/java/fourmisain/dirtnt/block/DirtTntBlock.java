package fourmisain.dirtnt.block;

import fourmisain.dirtnt.Dirtable;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class DirtTntBlock extends TntBlock {
	public static BlockBehaviour.Properties getDefaultProperties() {
		return BlockBehaviour.Properties.of()
			.mapColor(MapColor.FIRE)
			.instabreak()
			.sound(SoundType.GRASS)
			.ignitedByLava()
			.isRedstoneConductor(Blocks::never);
	}

	public DirtTntBlock(BlockBehaviour.Properties properties, Identifier dirtType) {
		super(properties);
		((Dirtable) this).makeDirty(dirtType);
	}
}
