package com.szan;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpecterCraftClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpecterCraft/Client");

    private boolean wasRightPressed = false;

    @Override
    public void onInitializeClient() {
        LOGGER.info("============================================");
        LOGGER.info("[SpecterCraft Client] Inicjalizacja...");
        LOGGER.info("============================================");
    }
}