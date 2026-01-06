package com.szan.handler;

import net.fabricmc.fabric. api.client. event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RightClickCanceller {
    private static final Logger LOGGER = LoggerFactory. getLogger("SpecterCraft/RightClickCanceller");

    private static boolean lookingAtItem = false;
    private static int cooldownTicks = 0;

    /**
     * Ustaw cooldown na określoną liczbę ticków
     */
    public static void setCooldown(int ticks) {
        cooldownTicks = ticks;
        LOGGER.info("[RightClickCanceller] ✓ Cooldown ustawiony:  {} ticków", ticks);
    }

    /**
     * Anuluj PPM na 10 ticków (dla starego systemu)
     */
    public static void cancelNextRightClick() {
        setCooldown(10);
        LOGGER.info("[RightClickCanceller] ✓ Anulowanie PPM (stary system)");
    }

    /**
     * Przywróć PPM (wyzeruj cooldown)
     */
    public static void restore() {
        cooldownTicks = 0;
        lookingAtItem = false;
        LOGGER.info("[RightClickCanceller] ✓ PPM przywrócony");
    }

    /**
     * Ustaw czy gracz patrzy na item
     */
    public static void setLookingAtItem(boolean looking) {
        lookingAtItem = looking;
    }

    /**
     * Sprawdź czy anulować PPM
     * - Gdy patrzysz na item (nowy system)
     * - Lub gdy cooldown aktywny (stary system)
     */
    public static boolean shouldCancel() {
        return lookingAtItem || cooldownTicks > 0;
    }

    /**
     * Sprawdź czy patrzysz na item
     */
    public static boolean isLookingAtItem() {
        return lookingAtItem;
    }

    /**
     * Zarejestruj tick event
     */
    public static void register() {
        LOGGER.info("[RightClickCanceller] Rejestrowanie handlera.. .");

        // Tick event - zmniejszaj cooldown
        ClientTickEvents. END_CLIENT_TICK.register(client -> {
            if (cooldownTicks > 0) {
                cooldownTicks--;

                if (cooldownTicks == 0) {
                    LOGGER.debug("[RightClickCanceller] Cooldown wygasł");
                }
            }
        });

        LOGGER.info("[RightClickCanceller] ✓ Zarejestrowany!");
    }
}