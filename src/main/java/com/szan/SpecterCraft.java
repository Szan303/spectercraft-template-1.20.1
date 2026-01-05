package com.szan;

import com. szan.handler.CancelAutoPickup;
import com.szan.handler.CustomScreenHandler;
import com.szan.handler.NetworkHandler;
import com. szan.handler.PlayerDroppedItemTracker;
import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle. FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util. Identifier;
import org.slf4j.Logger;
import org. slf4j.LoggerFactory;

public class SpecterCraft implements ModInitializer {
    public static final String MOD_ID = "spectercraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Packet IDs
    public static final Identifier PICKUP_PACKET_ID = new Identifier(MOD_ID, "pickup");
    public static final Identifier RECIPE_LIST_PACKET_ID = new Identifier(MOD_ID, "recipe_list");
    public static final Identifier RECIPE_SELECTED_PACKET_ID = new Identifier(MOD_ID, "recipe_selected");
    public static final Identifier SLOT_SWAP_PACKET_ID = new Identifier(MOD_ID, "slot_swap");

    // Screen Handler Type - POPRAWIONA REJESTRACJA
    public static final ScreenHandlerType<CustomScreenHandler> CUSTOM_SCREEN_HANDLER =
            Registry.register(
                    Registries.SCREEN_HANDLER,
                    new Identifier(MOD_ID, "custom_inventory"),
                    new ScreenHandlerType<>(CustomScreenHandler::new, FeatureFlags.VANILLA_FEATURES)  // ← DODANE!
            );

    @Override
    public void onInitialize() {
        LOGGER.info("============================================");
        LOGGER.info("[SpecterCraft] Inicjalizacja moda.. .");
        LOGGER.info("============================================");

        CancelAutoPickup.register();
        NetworkHandler.register();
        PlayerDroppedItemTracker. register();

        LOGGER.info("[SpecterCraft] ✓ Screen handler:  {}", CUSTOM_SCREEN_HANDLER);

        LOGGER.info("============================================");
        LOGGER.info("[SpecterCraft] ✓ Mod załadowany pomyślnie!");
        LOGGER.info("============================================");
    }
}