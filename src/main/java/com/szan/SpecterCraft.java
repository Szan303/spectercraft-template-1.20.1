package com.szan;

import com.szan.handler.CancelAutoPickup;
import com.szan.handler.NetworkHandler;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.szan.handler.PlayerDroppedItemTracker;

public class SpecterCraft implements ModInitializer {
    public static final String MOD_ID = "spectercraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Packet ID dla komunikacji klient-serwer
    public static final Identifier PICKUP_PACKET_ID = new Identifier(MOD_ID, "pickup");
    public static final Identifier RECIPE_LIST_PACKET_ID = new Identifier(MOD_ID, "recipe_list");
    public static final Identifier RECIPE_SELECTED_PACKET_ID = new Identifier(MOD_ID, "recipe_selected");

    @Override
    public void onInitialize() {
        LOGGER.info("============================================");
        LOGGER.info("[SpecterCraft] Inicjalizacja moda.. .");
        LOGGER.info("============================================");

        // Zarejestruj handler blokowania auto-pickupu
        CancelAutoPickup.register();

        // Zarejestruj handler packetów
        NetworkHandler.register();
        PlayerDroppedItemTracker.register();
        LOGGER. info("============================================");
        LOGGER.info("[SpecterCraft] ✓ Mod załadowany pomyślnie!");
        LOGGER.info("============================================");
    }
}