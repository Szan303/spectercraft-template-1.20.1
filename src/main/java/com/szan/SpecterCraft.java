package com.szan;

import com.szan.registry.block.ModBlockEntities;
import com.szan.registry.block.ModBlocks;
import com.szan.registry.item.ModItemGroups;
import com.szan.registry.item.ModItems;
import com.szan.registry.recipe.ModRecipeSerializers;
import com.szan.registry.recipe.ModRecipeTypes;
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
        ModRecipeTypes.BRICK_SMELTING.toString(); // niech zostanie lub przenieś do init, byle wykonało się PRZED rejestracją przepisów!
        ModRecipeSerializers.register();        // <-- Musi być tutaj!
        ModBlocks.register();
        ModItems.register();
        ModBlockEntities.register();
        ModItemGroups.register();

        LOGGER.info("============================================");
        LOGGER.info("[SpecterCraft] ✓ Mod załadowany pomyślnie!");
        LOGGER.info("============================================");
    }
}