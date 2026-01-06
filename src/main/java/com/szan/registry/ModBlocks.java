package com.szan.registry;

import com.szan.SpecterCraft;
import com. szan.block.WetClayBlock;
import net.fabricmc.fabric.api.object. builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block. Blocks;
import net.minecraft. item.BlockItem;
import net. minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft. registry.Registry;
import net. minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import org. slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModBlocks {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpecterCraft/Registry/Blocks");

    // ========== BLOCK DEFINITIONS ==========
    public static final Block WET_CLAY = registerBlock("wet_clay.json",
            new WetClayBlock(FabricBlockSettings.copyOf(Blocks.CLAY)
                    .strength(0.6f)
                    .sounds(BlockSoundGroup.GRAVEL)
            )
    );

    // Dodaj więcej bloków tutaj...

    // ========== HELPER METHODS ==========

    /**
     * Rejestruje blok + automatycznie tworzy BlockItem
     */
    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries. BLOCK, new Identifier(SpecterCraft.MOD_ID, name), block);
    }

    /**
     * Rejestruje BlockItem dla bloku
     */
    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, new Identifier(SpecterCraft.MOD_ID, name),
                new BlockItem(block, new Item.Settings()));
    }

    /**
     * Wywołaj to w onInitialize()
     */
    public static void register() {
        LOGGER.info("Rejestrowanie bloków...");
        // Bloki są rejestrowane przy inicjalizacji static fields
    }
}