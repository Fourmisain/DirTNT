package fourmisain.dirtnt;

import fourmisain.dirtnt.block.DirtTntBlock;
import fourmisain.dirtnt.entity.DirtTntEntity;
import fourmisain.dirtnt.mixin.FireBlockAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirTnt implements ModInitializer {
	public static final String MOD_ID = "dirtnt";

	public static final List<Block> DIRT_TYPES = List.of(Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.STONE, Blocks.COBBLESTONE, Blocks.DIAMOND_BLOCK);

	// used to override TntBlock.primeTnt() behavior
	public static Block dirtyOverride = Blocks.AIR;

	public static Map<Block, Block> BLOCK_MAP = new HashMap<>();
	public static Map<Block, Item> ITEM_MAP = new HashMap<>();
	public static Map<Block, EntityType<DirtTntEntity>> ENTITY_TYPE_MAP = new HashMap<>();

	public static Identifier id(String id) {
		return new Identifier(MOD_ID, id);
	}

	@Override
	public void onInitialize() {
		FireBlockAccessor fireBlock = (FireBlockAccessor)Blocks.FIRE;

		for (Block dirtType : DIRT_TYPES) {
			Identifier id = id(Registry.BLOCK.getId(dirtType).getPath() + "_tnt");

			DirtTntBlock block = Registry.register(Registry.BLOCK, id, new DirtTntBlock(dirtType));
			BlockItem item = Registry.register(Registry.ITEM, id, new BlockItem(block, new FabricItemSettings().group(ItemGroup.REDSTONE)));
			EntityType<DirtTntEntity> entityType = Registry.register(Registry.ENTITY_TYPE, id, getEntityType(dirtType));

			BLOCK_MAP.put(dirtType, block);
			ITEM_MAP.put(dirtType, item);
			ENTITY_TYPE_MAP.put(dirtType, entityType);

			DispenserBlock.registerBehavior(item, (pointer, stack) -> DirTnt.dispense(dirtType, pointer, stack));

			fireBlock.invokeRegisterFlammableBlock(block, 15, 100);
		}
	}

	private static ItemStack dispense(Block dirtType, BlockPointer pointer, ItemStack stack) {
		World world = pointer.getWorld();
		BlockPos pos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
		DirtTntEntity tntEntity = new DirtTntEntity(dirtType, world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
		world.spawnEntity(tntEntity);
		world.playSound(null, tntEntity.getX(), tntEntity.getY(), tntEntity.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
		world.emitGameEvent(null, GameEvent.ENTITY_PLACE, pos);
		stack.decrement(1);
		return stack;
	}

	private EntityType<DirtTntEntity> getEntityType(Block dirtType) {
		return FabricEntityTypeBuilder.create()
				.<DirtTntEntity>entityFactory((entityType, world) -> new DirtTntEntity(dirtType, entityType, world))
				.spawnGroup(SpawnGroup.MISC)
				.fireImmune()
				.dimensions(EntityDimensions.fixed(0.98F, 0.98F))
				.trackRangeBlocks(10)
				.trackedUpdateRate(10)
				.build();
	}
}
