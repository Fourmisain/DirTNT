package fourmisain.dirtnt;

import fourmisain.dirtnt.block.DirtTntBlock;
import fourmisain.dirtnt.entity.DirtTntEntity;
import fourmisain.dirtnt.mixin.FireBlockAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.*;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class DirTnt implements ModInitializer {
	public static final String MOD_ID = "dirtnt";

	// used to override TntBlock.primeTnt() behavior
	public static boolean dirtyOverride = false;

	public static Block DIRT_TNT_BLOCK;
	public static Item DIRT_TNT_ITEM;
	public static EntityType<DirtTntEntity> DIRT_TNT_ENTITY_TYPE;

	public static Identifier id(String id) {
		return new Identifier(MOD_ID, id);
	}

	@Override
	public void onInitialize() {
		DIRT_TNT_BLOCK = Registry.register(Registry.BLOCK, id("dirt_tnt"), new DirtTntBlock());

		DIRT_TNT_ITEM = Registry.register(Registry.ITEM, id("dirt_tnt"), new BlockItem(DIRT_TNT_BLOCK, new FabricItemSettings().group(ItemGroup.REDSTONE)));

		DIRT_TNT_ENTITY_TYPE = Registry.register(Registry.ENTITY_TYPE, id("dirt_tnt"), FabricEntityTypeBuilder.create()
				.<DirtTntEntity>entityFactory(DirtTntEntity::new)
				.spawnGroup(SpawnGroup.MISC)
				.fireImmune()
				.dimensions(EntityDimensions.fixed(0.98F, 0.98F))
				.trackRangeBlocks(10)
				.trackedUpdateRate(10)
				.build());

		DispenserBlock.registerBehavior(DirTnt.DIRT_TNT_ITEM, (BlockPointer pointer, ItemStack stack) -> {
			World world = pointer.getWorld();
			BlockPos pos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
			DirtTntEntity tntEntity = new DirtTntEntity(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			world.spawnEntity(tntEntity);
			world.playSound(null, tntEntity.getX(), tntEntity.getY(), tntEntity.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
			world.emitGameEvent(null, GameEvent.ENTITY_PLACE, pos);
			stack.decrement(1);
			return stack;
		});

		FireBlockAccessor fireBlock = (FireBlockAccessor)Blocks.FIRE;
		fireBlock.invokeRegisterFlammableBlock(DIRT_TNT_BLOCK, 15, 100);
	}
}
