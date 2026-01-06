package com.szan;

import com.szan.handler.CancelAutoPickup;
import com.szan.handler. NetworkHandler;
import com.szan.handler.PlayerDroppedItemTracker;
import com.szan.registry.*;
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
        ModPackets.register();
        ModBlocks.register();
        ModItems.register();
        ModBlockEntities.register();
        ModItemGroups.register();

        // Handlers
        CancelAutoPickup.register();
        NetworkHandler.register();
        PlayerDroppedItemTracker. register();

        LOGGER.info("============================================");
        LOGGER.info("[SpecterCraft] ✓ Mod załadowany pomyślnie!");
        LOGGER.info("============================================");
    }
}