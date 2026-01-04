package com.szan;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpecterCraftClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("spectercraft");
    
    @Override
    public void onInitializeClient() {
        LOGGER.info("SpecterCraft Client initialized!");
    }
}
