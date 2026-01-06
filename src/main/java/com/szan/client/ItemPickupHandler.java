package com.szan.client;

import com.szan.client.screen. CustomInventoryScreen;
import com.szan.handler.RightClickCanceller;
import com.szan.registry.ModPackets;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client. networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.ItemEntity;
import net. minecraft.network.PacketByteBuf;
import net.minecraft.util. Hand;
import net.minecraft. util.math.Vec3d;
import org.slf4j. Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util. concurrent.ExecutorService;
import java.util. concurrent.Executors;

public class ItemPickupHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpecterCraft/ItemPickup");
    private static final ExecutorService ASYNC_EXECUTOR = Executors.newFixedThreadPool(2);

    public static void handleRightClick(MinecraftClient client) {
        if (client.player == null || client.world == null) return;
        if (client.currentScreen != null) return; // Ignoruj gdy GUI otwarte

        Vec3d cameraPos = client.player.getCameraPosVec(1.0F);
        Vec3d lookVec = client.player. getRotationVec(1.0F);
        double maxReach = 4.5;

        // Skopiuj listę itemów (żeby wątek nie dotykał client. world)
        List<ItemEntity> nearbyItems = new ArrayList<>(client.world.getEntitiesByClass(
                ItemEntity.class,
                client.player.getBoundingBox().expand(maxReach),
                entity -> !entity.isRemoved()
        ));

        LOGGER.debug("[ItemPickup] Znaleziono {} itemów do przetworzenia", nearbyItems.size());

        // ASYNC: Znajdź najbliższy item (w tle)
        CompletableFuture. supplyAsync(() -> {
            return findNearestItem(nearbyItems, cameraPos, lookVec, maxReach);
        }, ASYNC_EXECUTOR).thenAcceptAsync(target -> {
            // SYNC: Wyślij packet (musi być w main thread)
            client.execute(() -> {
                pickupItem(client, target);
            });
        });
    }

    /**
     * ASYNC: Znajdź najbliższy ItemEntity (nie blokuje gry)
     */
    private static ItemEntity findNearestItem(List<ItemEntity> items, Vec3d cameraPos, Vec3d lookVec, double maxReach) {
        long startTime = System.nanoTime();

        ItemEntity closest = null;
        double closestDistance = maxReach;

        for (ItemEntity item : items) {
            Vec3d itemPos = item.getPos().add(0, item.getHeight() / 2, 0);
            Vec3d toItem = itemPos.subtract(cameraPos);
            double distance = toItem.length();

            if (distance > maxReach) {
                continue;
            }

            Vec3d toItemNormalized = toItem.normalize();
            double dotProduct = lookVec.dotProduct(toItemNormalized);

            if (dotProduct > 0.95 && distance < closestDistance) {
                closest = item;
                closestDistance = distance;
            }
        }

        long duration = System.nanoTime() - startTime;
        LOGGER. debug("[ItemPickup] Raycast zajął {} μs", duration / 1000);

        return closest;
    }

    /**
     * SYNC: Obsłuż znaleziony item (main thread)
     */
    private static void pickupItem(MinecraftClient client, ItemEntity target) {
        if (client.currentScreen instanceof CustomInventoryScreen) {
            LOGGER.debug("[ItemPickup] Inventory otwarty - ignoruję PPM");
            return;
        }

        LOGGER.info("========================================");
        LOGGER.info("[ItemPickup] Wynik async raycast");
        LOGGER.info("[ItemPickup] Znaleziony ItemEntity: {}", target);

        if (target != null && ! target.isRemoved()) {
            boolean isCrafting = Screen.hasShiftDown();

            LOGGER.info("[ItemPickup] ✓ Target to ItemEntity!");
            LOGGER.info("[ItemPickup] Crafting: {}", isCrafting);
            LOGGER.info("[ItemPickup] EntityID:  {}", target.getId());
            LOGGER.info("[ItemPickup] Stack: {}", target.getStack());

            // Wyślij packet do serwera (MUSI być main thread)
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeInt(target. getId());
            buf.writeBoolean(isCrafting);
            ClientPlayNetworking.send(ModPackets.PICKUP, buf);

            client.player. swingHand(Hand. MAIN_HAND);

            // Anuluj vanilla PPM
            RightClickCanceller.cancelNextRightClick();

            LOGGER.info("[ItemPickup] ✓ Packet wysłany + PPM anulowany!");
        } else {
            LOGGER.info("[ItemPickup] ✗ Brak ItemEntity w zasięgu wzroku");
        }

        LOGGER.info("========================================");
    }
}