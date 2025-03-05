package fourmisain.dirtnt;

import fourmisain.dirtnt.block.DirtTntBlock;
import fourmisain.dirtnt.config.DirTntConfig;
import fourmisain.dirtnt.config.GsonConfigHelper;
import fourmisain.dirtnt.entity.DirtTntEntity;
import fourmisain.dirtnt.mixin.FireBlockAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class DirTnt implements ModInitializer {
	public static final String MOD_ID = "dirtnt";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public static final Set<Identifier> DIRT_TYPES = new LinkedHashSet<>();

	// used to override TntBlock.primeTnt() behavior
	public static Identifier dirtyOverride = null;

	public static final Map<Identifier, DirtTntBlock> BLOCK_MAP = new HashMap<>();
	public static final Map<Identifier, Item> ITEM_MAP = new HashMap<>();
	public static final Map<Identifier, EntityType<DirtTntEntity>> ENTITY_TYPE_MAP = new HashMap<>();

	public static Identifier id(String id) {
		return Identifier.of(MOD_ID, id);
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

		for (Identifier dirtType : DIRT_TYPES) {
			Identifier id = getDirtTntBlockId(dirtType);

			// register dirt tnt
			var block = Registry.register(Registries.BLOCK, id,
				new DirtTntBlock(DirtTntBlock.getDefaultSettings().registryKey(RegistryKey.of(RegistryKeys.BLOCK, id)), dirtType));
			var item = Items.register(block, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, id)));
			var entityType = Registry.register(Registries.ENTITY_TYPE, id, createDirtTntEntityType(dirtType, id));
			BLOCK_MAP.put(dirtType, block);
			ITEM_MAP.put(dirtType, item);
			ENTITY_TYPE_MAP.put(dirtType, entityType);

			DispenserBlock.registerBehavior(item, (pointer, stack) -> dispenseDirtTnt(dirtType, pointer, stack));

			fireBlock.invokeRegisterFlammableBlock(block, 15, 100);
		}

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> {
			for (Identifier dirtType : DIRT_TYPES) {
				entries.add(ITEM_MAP.get(dirtType));
			}
		});
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

	private EntityType<DirtTntEntity> createDirtTntEntityType(Identifier dirtType, Identifier id) {
		return EntityType.Builder.<DirtTntEntity>create((entityType, world) -> new DirtTntEntity(dirtType, entityType, world), SpawnGroup.MISC)
			.dropsNothing()
			.makeFireImmune()
			.dimensions(0.98F, 0.98F)
			.eyeHeight(0.15F)
			.maxTrackingRange(10)
			.trackingTickInterval(10)
			.build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, id));
	}

	public static String getRecipeJson(Identifier dirtType, Identifier itemId) {
		return """
			{
			  "type": "minecraft:crafting_shaped",
			  "category": "redstone",
			  "pattern": [
			    "###",
			    "#X#",
			    "###"
			  ],
			  "key": {
			    "#": "%s",
			    "X": "minecraft:tnt"
			  },
			  "result": {
			    "count": 1,
			    "id": "%s"
			  }
			}
			""".formatted(dirtType, itemId);
	}

	public static String getLootTableJson(Identifier blockId) {
		return """
			{
			  "type": "minecraft:block",
			  "pools": [
			    {
			      "conditions": [
			        {
			          "condition": "minecraft:survives_explosion"
			        }
			      ],
			      "entries": [
			        {
			          "type": "minecraft:item",
			          "conditions": [
			            {
			              "block": "%s",
			              "condition": "minecraft:block_state_property",
			              "properties": {
			                "unstable": "false"
			              }
			            }
			          ],
			          "name": "%s"
			        }
			      ],
			      "rolls": 1
			    }
			  ]
			}
			""".formatted(blockId, blockId);
	}
}
