package com.szan;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import com.szan.work.ManualPickupHandler;

public class SpecterCraft implements ModInitializer {
    public static final String MOD_ID = "spectercraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
//        ManualPickupHandler.register();
        LOGGER.info("Hello Fabric world!");
    }
}