package fourmisain.dirtnt;

import fourmisain.dirtnt.block.DirtTntBlock;
import fourmisain.dirtnt.config.DirTntConfig;
import fourmisain.dirtnt.config.GsonConfigHelper;
import fourmisain.dirtnt.entity.PrimedDirtTnt;
import fourmisain.dirtnt.mixin.BlocksAccessor;
import fourmisain.dirtnt.mixin.FireBlockAccessor;
import fourmisain.dirtnt.mixin.ItemsAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;
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
	public static final Map<Identifier, EntityType<PrimedDirtTnt>> ENTITY_TYPE_MAP = new HashMap<>();

	public static Identifier id(String id) {
		return Identifier.fromNamespaceAndPath(MOD_ID, id);
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
		if (config.enableAll) DIRT_TYPES.addAll(BuiltInRegistries.BLOCK.keySet()); // experimental option
	}

	@Override
	public void onInitialize() {
		loadConfig();

		FireBlockAccessor fireBlock = (FireBlockAccessor)Blocks.FIRE;

		for (Identifier dirtType : DIRT_TYPES) {
			Identifier id = getDirtTntBlockId(dirtType);

			// register dirt tnt
			var blockItemId = BlockItemId.create(id, id);

			var block = (DirtTntBlock) BlocksAccessor.invokeRegister(blockItemId.block(), p -> new DirtTntBlock(p, dirtType), DirtTntBlock.getDefaultProperties());
			var item = ItemsAccessor.invokeRegisterBlock(blockItemId, block);
			var entityType = Registry.register(BuiltInRegistries.ENTITY_TYPE, id, createDirtTntEntityType(dirtType, id));
			BLOCK_MAP.put(dirtType, block);
			ITEM_MAP.put(dirtType, item);
			ENTITY_TYPE_MAP.put(dirtType, entityType);

			DispenserBlock.registerBehavior(item, (pointer, stack) -> dispenseDirtTnt(dirtType, pointer, stack));

			fireBlock.invokeSetFlammable(block, 15, 100);
		}

		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.REDSTONE_BLOCKS).register(entries -> {
			for (Identifier dirtType : DIRT_TYPES) {
				entries.accept(ITEM_MAP.get(dirtType));
			}
		});
	}

	private static ItemStack dispenseDirtTnt(Identifier dirtType, BlockSource pointer, ItemStack stack) {
		Level level = pointer.level();
		BlockPos pos = pointer.pos().relative(pointer.state().getValue(DispenserBlock.FACING));
		PrimedDirtTnt tnt = new PrimedDirtTnt(dirtType, level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
		level.addFreshEntity(tnt);
		level.playSound(null, tnt.getX(), tnt.getY(), tnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
		level.gameEvent(null, GameEvent.ENTITY_PLACE, pos);
		stack.shrink(1);
		return stack;
	}

	private EntityType<PrimedDirtTnt> createDirtTntEntityType(Identifier dirtType, Identifier id) {
		return EntityType.Builder.<PrimedDirtTnt>of((entityType, level) -> new PrimedDirtTnt(dirtType, entityType, level), MobCategory.MISC)
			.noLootTable()
			.fireImmune()
			.sized(0.98F, 0.98F)
			.eyeHeight(0.15F)
			.clientTrackingRange(10)
			.updateInterval(10)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, id));
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
