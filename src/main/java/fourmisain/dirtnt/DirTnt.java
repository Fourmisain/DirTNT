package fourmisain.dirtnt;

import fourmisain.dirtnt.entity.DirtTntEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.*;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

// TODO add recipe

@SuppressWarnings("ConstantConditions")
public class DirTnt implements ModInitializer {
    public static final String MOD_ID = "dirtnt";

    public static Block DIRT_TNT_BLOCK;
    public static Item DIRT_TNT_ITEM;
    public static EntityType<DirtTntEntity> DIRT_TNT_ENTITY_TYPE;

    public static Identifier id(String id) {
        return new Identifier(MOD_ID, id);
    }

    @Override
    public void onInitialize() {
        // use a dirty TNT block
        TntBlock dirtTntBlock = new TntBlock(FabricBlockSettings.of(Material.TNT).breakInstantly().sounds(BlockSoundGroup.GRASS));
        ((Dirtable) dirtTntBlock).makeDirty();
        DIRT_TNT_BLOCK = Registry.register(Registry.BLOCK, id("dirt_tnt"), dirtTntBlock);

        DIRT_TNT_ITEM = Registry.register(Registry.ITEM, id("dirt_tnt"), new BlockItem(DIRT_TNT_BLOCK, new FabricItemSettings().group(ItemGroup.REDSTONE)));

        DIRT_TNT_ENTITY_TYPE = Registry.register(Registry.ENTITY_TYPE, id("dirt_tnt"), FabricEntityTypeBuilder.create()
            .<DirtTntEntity>entityFactory(DirtTntEntity::new)
            .spawnGroup(SpawnGroup.MISC)
            .fireImmune()
            .dimensions(EntityDimensions.fixed(0.98F, 0.98F))
            .trackRangeBlocks(10)
            .trackedUpdateRate(10)
            .build());
    }
}
