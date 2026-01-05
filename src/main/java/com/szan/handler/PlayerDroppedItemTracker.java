package com.szan. handler;

import net.minecraft.entity.ItemEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerDroppedItemTracker {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpecterCraft/ItemTracker");
    public static final String PLAYER_DROPPED_TAG = "spectercraft_player_dropped";

    public static void register() {
        LOGGER.info("Rejestrowanie PlayerDroppedItemTracker.. .");

        // Ten handler nie potrzebuje eventów, tylko utility methods

        LOGGER.info("✓ PlayerDroppedItemTracker zarejestrowany!");
    }

    /**
     * Oznacz item jako upuszczony przez gracza (nie despawnuje się)
     */
    public static void markAsPlayerDropped(ItemEntity itemEntity) {
        itemEntity.addCommandTag(PLAYER_DROPPED_TAG);
        LOGGER.debug("Item {} oznaczony jako player-dropped",
                itemEntity.getStack().getItem().getName().getString());
    }

    /**
     * Sprawdź czy item został upuszczony przez gracza
     */
    public static boolean isPlayerDropped(ItemEntity itemEntity) {
        return itemEntity.getCommandTags().contains(PLAYER_DROPPED_TAG);
    }
}