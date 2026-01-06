package com.szan.handler;

import net.fabricmc.fabric.api. client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api. event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.util.ActionResult;
import net.minecraft.util. TypedActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RightClickCanceller {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpecterCraft/RightClickCanceller");

    private static int cancelTimer = 0;

    /**
     * Anuluje PPM natychmiast (prewencyjnie)
     */
    public static void cancelNextRightClick() {
        cancelTimer = 10; // 10 ticków = 500ms (wystarczy na raycast)
        LOGGER.info("[RightClickCanceller] ✓ Anulowanie:  {} ticków", cancelTimer);
    }

    /**
     * Przywróć PPM (gdy nie ma itemu)
     */
    public static void restore() {
        cancelTimer = 0;
        LOGGER.info("[RightClickCanceller] ✓ PPM przywrócony");
    }

    /**
     * Sprawdź czy anulowanie aktywne
     */
    public static boolean isActive() {
        return cancelTimer > 0;
    }

    public static void register() {
        LOGGER.info("Rejestrowanie RightClickCanceller.. .");

        // Tick event - zmniejszaj timer
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (cancelTimer > 0) {
                cancelTimer--;
                if (cancelTimer == 0) {
                    LOGGER.debug("[RightClickCanceller] Timer wygasł");
                }
            }
        });

        // Anuluj UseItem (PPM na item w ręce)
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient && cancelTimer > 0) {
                LOGGER. info("[RightClickCanceller] ✓ UseItem ANULOWANY!");
                return TypedActionResult.fail(player. getStackInHand(hand));
            }
            return TypedActionResult.pass(player.getStackInHand(hand));
        });

        // Anuluj UseBlock (PPM na blok/powietrze)
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient && cancelTimer > 0) {
                LOGGER.info("[RightClickCanceller] ✓ UseBlock ANULOWANY!");
                return ActionResult. FAIL;
            }
            return ActionResult.PASS;
        });
    }
}