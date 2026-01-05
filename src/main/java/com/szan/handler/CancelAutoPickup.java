package com.szan.handler;

import net.fabricmc.fabric.api. event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event. lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.ItemEntity;
import net.minecraft. server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CancelAutoPickup {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpecterCraft/CancelAutoPickup");

    public static void register() {
        LOGGER.info("Rejestrowanie CancelAutoPickup...");

        // Gdy ItemEntity się spawni, ustaw nieskończony pickup delay
        ServerEntityEvents. ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof ItemEntity itemEntity) {
                itemEntity.setPickupDelay(Integer.MAX_VALUE);
                LOGGER.debug("ItemEntity załadowany z pickup delay = MAX");
            }
        });

        // W trybie kreatywnym pozwól na pickup w okolicy gracza
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server. getPlayerManager().getPlayerList()) {
                if (player.isCreative()) {
                    Box area = player.getBoundingBox().expand(1.0);

                    for (ItemEntity item : player.getServerWorld().getEntitiesByClass(ItemEntity.class, area, e -> true)) {
                        item.setPickupDelay(0);
                    }
                }
            }
        });

        LOGGER.info("✓ CancelAutoPickup zarejestrowany!");
    }
}