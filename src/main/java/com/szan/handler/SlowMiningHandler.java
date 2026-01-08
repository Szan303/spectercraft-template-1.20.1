package com.szan.handler;

import net.minecraft.entity.player.PlayerEntity;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimalny handler do kontroli spowolnienia kopania.
 * - Możesz włączyć/wyłączyć spowolnienie globalnie lub per-player (tu jest przykład per-player).
 * - Multiplier domyślnie 0.5 (50% prędkości).
 */
public class SlowMiningHandler {
    private static final Set<UUID> slowedPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static volatile float speedMultiplier = 0.5f; // 50% prędkości

    /** Zwraca true jeśli dany gracz ma nałożone spowolnienie kopania */
    public static boolean isSlowed(PlayerEntity player) {
        if (player == null) return false;
        return slowedPlayers.contains(player.getUuid());
    }

    /** Włącz spowolnienie dla gracza (server-side) */
    public static void setSlowed(PlayerEntity player, boolean slowed) {
        if (player == null) return;
        if (slowed) slowedPlayers.add(player.getUuid());
        else slowedPlayers.remove(player.getUuid());
    }

    /** Ustaw globalny mnożnik prędkości (np. 0.6f = 60% prędkości) */
    public static void setSpeedMultiplier(float mul) {
        speedMultiplier = mul;
    }

    public static float getSpeedMultiplier() {
        return speedMultiplier;
    }

    /** Wyłącz wszystkie spowolnienia (debug) */
    public static void clearAll() {
        slowedPlayers.clear();
    }
}