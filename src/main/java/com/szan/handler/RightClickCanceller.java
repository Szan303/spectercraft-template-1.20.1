package com.szan.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RightClickCanceller {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpecterCraft/RightClickCanceller");

    private static boolean shouldCancelNextRightClick = false;
    private static long cancelUntilTime = 0;

    /**
     * Anuluj następny PPM (wywołane przez SpecterCraftClient po pomyślnym pickup)
     */
    public static void cancelNextRightClick() {
        shouldCancelNextRightClick = true;
        cancelUntilTime = System.currentTimeMillis() + 100;  // 100ms window
        LOGGER.info("[RightClickCanceller] Następny PPM zostanie anulowany (100ms window)");
    }

    /**
     * Sprawdź czy należy anulować PPM (wywołane przez mixin)
     */
    public static boolean shouldCancel() {
        if (shouldCancelNextRightClick && System.currentTimeMillis() < cancelUntilTime) {
            shouldCancelNextRightClick = false;
            return true;
        }
        return false;
    }
}