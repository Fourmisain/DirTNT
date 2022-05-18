package fourmisain.dirtnt;

import com.google.gson.JsonObject;
import fourmisain.dirtnt.block.DirtTntBlock;
import fourmisain.dirtnt.entity.DirtTntEntity;
import fourmisain.dirtnt.mixin.FireBlockAccessor;
import net.devtech.arrp.ARRP;
import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.loot.JPool;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static net.devtech.arrp.json.loot.JLootTable.*;

public class DirTnt implements ModInitializer {
	public static final String MOD_ID = "dirtnt";
	public static Logger LOGGER = LogManager.getLogger(MOD_ID);

	public static final RuntimeResourcePack RESOURCE_PACK = RuntimeResourcePack.create(DirTnt.id(MOD_ID));

	public static final List<Identifier> DIRT_TYPES = new ArrayList<>(Registry.BLOCK.getIds());
	// // Blocks.DIRT, Blocks.STONE, Blocks.COBBLESTONE

	// used to override TntBlock.primeTnt() behavior
	public static Identifier dirtyOverride = null;

	public static Map<Identifier, Block> BLOCK_MAP = new HashMap<>();
	public static Map<Identifier, Item> ITEM_MAP = new HashMap<>();
	public static Map<Identifier, EntityType<DirtTntEntity>> ENTITY_TYPE_MAP = new HashMap<>();

	public static Identifier id(String id) {
		return new Identifier(MOD_ID, id);
	}

	/** Block and Item ID */
	public static Identifier getDirtTntBlockId(Identifier dirtType) {
		String namespace = dirtType.getNamespace();

		if (namespace.equals("minecraft")) {
			return DirTnt.id(String.format("%s_tnt", dirtType.getPath()));
		} else {
			return DirTnt.id(String.format("%s_%s_tnt", namespace, dirtType.getPath()));
		}
	}

	@Override
	public void onInitialize() {
		FireBlockAccessor fireBlock = (FireBlockAccessor)Blocks.FIRE;
		JsonObject notUnstable = new JsonObject();
		notUnstable.addProperty("unstable", "false");

		for (Identifier dirtType : DIRT_TYPES) {
			Identifier id = getDirtTntBlockId(dirtType);

			// register dirt tnt
			DirtTntBlock block = Registry.register(Registry.BLOCK, id, new DirtTntBlock(dirtType));
			BlockItem item = Registry.register(Registry.ITEM, id, new BlockItem(block, new FabricItemSettings().group(ItemGroup.REDSTONE)));
			EntityType<DirtTntEntity> entityType = Registry.register(Registry.ENTITY_TYPE, id, createDirtTntEntityType(dirtType));

			BLOCK_MAP.put(dirtType, block);
			ITEM_MAP.put(dirtType, item);
			ENTITY_TYPE_MAP.put(dirtType, entityType);

			DispenserBlock.registerBehavior(item, (pointer, stack) -> dispenseDirtTnt(dirtType, pointer, stack));

			fireBlock.invokeRegisterFlammableBlock(block, 15, 100);

			RESOURCE_PACK.addRecipe()

			RESOURCE_PACK.addLootTable(DirTnt.id("blocks/" + id.getPath()),
					loot("minecraft:block")
							.pool(pool()
									.rolls(1)
									.bonus(0)
									.entry(entry()
											.type("minecraft:item")
											.name(id.toString())
											.condition(predicate("minecraft:block_state_property")
													.parameter("block", id.toString())
													.parameter("properties", notUnstable))
									)
									.condition(predicate("minecraft:survives_explosion"))));
		}

		RRPCallback.BEFORE_VANILLA.register(listener -> listener.add(RESOURCE_PACK));
	}

	private static ItemStack dispenseDirtTnt(Identifier dirtType, BlockPointer pointer, ItemStack stack) {
		World world = pointer.getWorld();
		BlockPos pos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
		DirtTntEntity tntEntity = new DirtTntEntity(dirtType, world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
		world.spawnEntity(tntEntity);
		world.playSound(null, tntEntity.getX(), tntEntity.getY(), tntEntity.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
		world.emitGameEvent(null, GameEvent.ENTITY_PLACE, pos);
		stack.decrement(1);
		return stack;
	}

	private EntityType<DirtTntEntity> createDirtTntEntityType(Identifier dirtType) {
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
