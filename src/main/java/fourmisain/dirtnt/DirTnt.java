package fourmisain.dirtnt;

import fourmisain.dirtnt.block.DirtTntBlock;
import fourmisain.dirtnt.config.DirTntConfig;
import fourmisain.dirtnt.config.GsonConfigHelper;
import fourmisain.dirtnt.entity.DirtTntEntity;
import fourmisain.dirtnt.mixin.FireBlockAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.TntBlock;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.*;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.loot.condition.SurvivesExplosionLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.solid.brrp.v1.api.RuntimeResourcePack;
import pers.solid.brrp.v1.fabric.api.RRPCallback;
import pers.solid.brrp.v1.tag.IdentifiedTagBuilder;

import java.io.IOException;
import java.util.*;

public class DirTnt implements ModInitializer {
	public static final String MOD_ID = "dirtnt";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public static final RuntimeResourcePack RESOURCE_PACK = RuntimeResourcePack.create(DirTnt.id(MOD_ID));

	public static final Set<Identifier> DIRT_TYPES = new LinkedHashSet<>();

	// used to override TntBlock.primeTnt() behavior
	public static Identifier dirtyOverride = null;

	public static final Map<Identifier, DirtTntBlock> BLOCK_MAP = new HashMap<>();
	public static final Map<Identifier, Item> ITEM_MAP = new HashMap<>();
	public static final Map<Identifier, EntityType<DirtTntEntity>> ENTITY_TYPE_MAP = new HashMap<>();

	public static Identifier id(String id) {
		return new Identifier(MOD_ID, id);
	}

	/** Block and Item ID */
	public static Identifier getDirtTntBlockId(Identifier dirtType) {
		String namespace = dirtType.getNamespace();

		if (namespace.equals("minecraft")) {
			return DirTnt.id(String.format("%s_tnt", dirtType.getPath()));
		} else {
			// to prevent most name collisions
			return DirTnt.id(String.format("%s_%s_tnt", namespace, dirtType.getPath()));
		}
	}

	public static void loadConfig() {
		DirTntConfig config = new DirTntConfig(); // load defaults

		GsonConfigHelper configHelper = new GsonConfigHelper(MOD_ID);
		if (configHelper.exists()) {
			// load config
			try {
				config = configHelper.load(DirTntConfig.class);
			} catch (IOException e) {
				LOGGER.error("couldn't load config", e);
			}
		} else {
			// save defaults
			try {
				configHelper.save(config);
			} catch (IOException e) {
				LOGGER.error("couldn't save config", e);
			}
		}

		// apply config
		DIRT_TYPES.clear();
		DIRT_TYPES.addAll(config.dirtTypes);
		if (config.enableAll) DIRT_TYPES.addAll(Registries.BLOCK.getIds()); // experimental option
	}

	@Override
	public void onInitialize() {
		loadConfig();

		FireBlockAccessor fireBlock = (FireBlockAccessor)Blocks.FIRE;
		IdentifiedTagBuilder<Block> endermanHoldableTagBuilder = IdentifiedTagBuilder.createBlock(BlockTags.ENDERMAN_HOLDABLE);

		for (Identifier dirtType : DIRT_TYPES) {
			Identifier id = getDirtTntBlockId(dirtType);

			// register dirt tnt
			DirtTntBlock block = Registry.register(Registries.BLOCK, id, new DirtTntBlock(dirtType));
			BlockItem item = Registry.register(Registries.ITEM, id, new BlockItem(block, new FabricItemSettings()));
			EntityType<DirtTntEntity> entityType = Registry.register(Registries.ENTITY_TYPE, id, createDirtTntEntityType(dirtType));
			BLOCK_MAP.put(dirtType, block);
			ITEM_MAP.put(dirtType, item);
			ENTITY_TYPE_MAP.put(dirtType, entityType);

			DispenserBlock.registerBehavior(item, (pointer, stack) -> dispenseDirtTnt(dirtType, pointer, stack));

			fireBlock.invokeRegisterFlammableBlock(block, 15, 100);

			// auto-gen recipe
			Optional<Item> dirt = Registries.ITEM.getOrEmpty(dirtType);
			if (dirt.isEmpty() || dirt.get() == Items.AIR) { // not every block has an associated item (and air is not a valid crafting ingredient)
				DirTnt.LOGGER.warn("can't auto-gen recipe for dirt type {}", dirtType);
			} else {
				RESOURCE_PACK.addRecipeAndAdvancement(id, ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, item, 1)
					.pattern("###")
					.pattern("#X#")
					.pattern("###")
					.input('#', dirt.get())
					.input('X', Items.TNT)
					.criterionFromItem(Items.TNT));
			}

			// auto-gen block loot table
			RESOURCE_PACK.addLootTable(DirTnt.id("blocks/" + id.getPath()), LootTable.builder()
				.pool(LootPool.builder()
					.conditionally(SurvivesExplosionLootCondition.builder())
					.rolls(ConstantLootNumberProvider.create(1.0F))
					.with(
						ItemEntry.builder(block)
							.conditionally(BlockStatePropertyLootCondition.builder(block)
								.properties(StatePredicate.Builder.create().exactMatch(TntBlock.UNSTABLE, false)))
					)));

			endermanHoldableTagBuilder.add(id);
		}

		RESOURCE_PACK.addTag(endermanHoldableTagBuilder);

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> {
			for (Identifier dirtType : DIRT_TYPES) {
				entries.add(ITEM_MAP.get(dirtType));
			}
		});

		RRPCallback.BEFORE_VANILLA.register(listener -> listener.add(RESOURCE_PACK));
	}

	private static ItemStack dispenseDirtTnt(Identifier dirtType, BlockPointer pointer, ItemStack stack) {
		World world = pointer.world();
		BlockPos pos = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
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
