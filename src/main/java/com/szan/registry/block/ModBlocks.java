package com.szan.registry.block;

import com.szan. SpecterCraft;
//import com.szan.block.BrickFurnace_old;
import com.szan.block.BrickFurnace;
import com.szan.block.WetClayBlock;
import com.szan.block.entity.BrickFurnaceEntity;
import net.fabricmc.fabric.api.object.builder.v1.block. FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block. Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft. item.BlockItem;
import net. minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net. minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModBlocks {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpecterCraft/Registry/Blocks");

    public static final Block WET_CLAY = registerBlock("wet_clay",
            new WetClayBlock(FabricBlockSettings.copyOf(Blocks.CLAY)
                    .strength(0.6f)
                    .sounds(BlockSoundGroup.GRAVEL)
            )
    );
    public static final Block BLOCK_IDD = registerBlock("block_idd",
            new Block(FabricBlockSettings.create()));

    public static final Block BRICK_FURNACE = registerBlock("brick_furnace",
            new BrickFurnace(FabricBlockSettings.copyOf(Blocks.FURNACE)
                    .strength(3.5f)
                    .sounds(BlockSoundGroup.STONE)
            )
    );

//    public static final BlockEntityType<BrickFurnaceEntity> BRICK_FURNACE_ENTITY = Registry.register(
//            Registries.BLOCK_ENTITY_TYPE,
//            new Identifier("spectercraft", "brick_furnace"),
//            FabricBlockEntityTypeBuilder.create(BrickFurnaceEntity::new, BRICK_FURNACE).build()
//    );

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(SpecterCraft.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, new Identifier(SpecterCraft.MOD_ID, name),
                new BlockItem(block, new Item.Settings()));
    }

    public static void register() {
        LOGGER.info("Rejestrowanie bloków...");
    }
}