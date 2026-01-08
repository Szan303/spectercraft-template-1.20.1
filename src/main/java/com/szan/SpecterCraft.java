package com.szan;

import com.szan.registry.Block.ModBlockEntities;
import com.szan.registry.Block.ModBlocks;
import com.szan.registry.DisabledRecipeRegistry;
import com.szan.registry.item.ModItemGroups;
import com.szan.registry.item.ModItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpecterCraft implements ModInitializer {
    public static final String MOD_ID = "spectercraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("============================================");
        LOGGER.info("[SpecterCraft] Inicjalizacja moda.. .");
        LOGGER.info("============================================");

        // Registry (kolejność WAŻNA!)
        ModBlocks.register();
        ModItems.register();
        ModBlockEntities.register();
        ModItemGroups.register();

        LOGGER.info("============================================");
        LOGGER.info("[SpecterCraft] ✓ Mod załadowany pomyślnie!");
        LOGGER.info("============================================");
    }
}