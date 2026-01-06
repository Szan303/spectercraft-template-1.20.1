package com.szan. client;

import com.szan. client.screen.CustomInventoryScreen;
import com.szan.handler.RightClickCanceller;
import com.szan.registry.ModPackets;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client. networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net. minecraft.client.gui.screen. Screen;
import net.minecraft.entity.ItemEntity;
import net.minecraft. network.PacketByteBuf;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util. concurrent.Executors;

public class ItemPickupHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpecterCraft/ItemPickup");
    private static final ExecutorService ASYNC_EXECUTOR = Executors.newFixedThreadPool(2);

    public static void handleRightClick(MinecraftClient client) {
        if (client.player == null || client.world == null) return;
        if (client.currentScreen != null) return;

        Vec3d cameraPos = client. player.getCameraPosVec(1.0F);
        Vec3d lookVec = client.player.getRotationVec(1.0F);
        double maxReach = 4.5;

        List<ItemEntity> nearbyItems = new ArrayList<>(client.world.getEntitiesByClass(
                ItemEntity.class,
                client.player.getBoundingBox().expand(maxReach),
                entity -> !entity.isRemoved()
        ));

        // INSTANT raycast (bez async)
        ItemEntity target = findNearestItem(nearbyItems, cameraPos, lookVec, maxReach);

        if (target != null) {
            // Anuluj PPM TYLKO jeśli znaleźliśmy item
            RightClickCanceller.cancelNextRightClick();
            pickupItem(client, target);
        }
        // Jeśli nie ma itemu, vanilla PPM działa normalnie
    }

    private static ItemEntity findNearestItem(List<ItemEntity> items, Vec3d cameraPos, Vec3d lookVec, double maxReach) {
        long startTime = System.nanoTime();

        ItemEntity closest = null;
        double closestDistance = maxReach;

        for (ItemEntity item : items) {
            Vec3d itemPos = item.getPos().add(0, item.getHeight() / 2, 0);
            Vec3d toItem = itemPos.subtract(cameraPos);
            double distance = toItem.length();

            if (distance > maxReach) continue;

            Vec3d toItemNormalized = toItem.normalize();
            double dotProduct = lookVec.dotProduct(toItemNormalized);

            if (dotProduct > 0.95 && distance < closestDistance) {
                closest = item;
                closestDistance = distance;
            }
        }

        long duration = System.nanoTime() - startTime;
        LOGGER.debug("[ItemPickup] Raycast:  {} μs", duration / 1000);

        return closest;
    }

    private static void pickupItem(MinecraftClient client, ItemEntity target) {
        if (client.currentScreen instanceof CustomInventoryScreen) {
            RightClickCanceller.restore();
            return;
        }

        LOGGER.info("========================================");
        LOGGER.info("[ItemPickup] Target: {}", target);

        if (target != null && ! target.isRemoved()) {
            boolean isCrafting = Screen.hasShiftDown();

            LOGGER.info("[ItemPickup] ✓ ItemEntity znaleziony!");
            LOGGER.info("[ItemPickup] Crafting: {}", isCrafting);
            LOGGER.info("[ItemPickup] EntityID: {}", target.getId());

            // Wyślij packet
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeInt(target.getId());
            buf.writeBoolean(isCrafting);
            ClientPlayNetworking.send(ModPackets.PICKUP, buf);

            // Animacja
            client.player.swingHand(Hand.MAIN_HAND);

            LOGGER.info("[ItemPickup] ✓ Packet wysłany!");
            // PPM pozostaje anulowany (timer zrobi reset)
        } else {
            LOGGER.info("[ItemPickup] ✗ Brak ItemEntity - przywracam PPM");
            RightClickCanceller.restore();
        }

        LOGGER.info("========================================");
    }
}