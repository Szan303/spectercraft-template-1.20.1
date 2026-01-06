//package com.szan.client;
//
//import net. fabricmc.fabric.api.event.player.UseEntityCallback;
//import net.minecraft.entity.ItemEntity;
//import net.minecraft. entity.player.PlayerInventory;
//import net.minecraft.item. ItemStack;
//import net.minecraft.server.network.ServerPlayerEntity;
//import net.minecraft.util.ActionResult;
//import net. minecraft.util.Hand;
//import org.slf4j.Logger;
//import org.slf4j. LoggerFactory;
//
//public class ManualPickupHandler {
//    private static final Logger LOGGER = LoggerFactory.getLogger("SpecterCraft/ManualPickup");
//
//    /**
//     * Rejestruje event handler dla manualnego podnoszenia itemów
//     */
//    public static void register() {
//        LOGGER.info("Rejestrowanie ManualPickupHandler.. .");
//
//        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
//            // Tylko na serwerze
//            if (world.isClient) {
//                return ActionResult.PASS;
//            }
//
//            // Tylko główna ręka
//            if (hand != Hand.MAIN_HAND) {
//                return ActionResult.PASS;
//            }
//
//            // Sprawdź czy to ItemEntity
//            if (entity instanceof ItemEntity itemEntity) {
//                return handleItemPickup(player, itemEntity);
//            }
//
//            return ActionResult.PASS;
//        });
//
//        LOGGER.info("✓ ManualPickupHandler zarejestrowany!");
//    }
//
//    /**
//     * Obsługuje podnoszenie itemu przez gracza
//     */
//    private static ActionResult handleItemPickup(net.minecraft. entity.player.PlayerEntity player, ItemEntity itemEntity) {
//        LOGGER.debug("[PICKUP] Gracz {} próbuje podnieść item", player.getName().getString());
//
//        PlayerInventory inventory = player.getInventory();
//        int selectedSlot = inventory.selectedSlot;
//        ItemStack handStack = inventory.getStack(selectedSlot);
//
//        LOGGER.debug("[PICKUP] Slot: {} | Pusta ręka: {}", selectedSlot, handStack.isEmpty());
//
//        // Sprawdź czy ręka jest pusta
//        if (! handStack.isEmpty()) {
//            LOGGER.debug("[PICKUP] ✗ Ręka zajęta - nie można podnieść");
//            return ActionResult. FAIL;
//        }
//
//        ItemStack itemStack = itemEntity.getStack();
//
//        LOGGER.info("[PICKUP] Podnoszę: {} x{}",
//                itemStack.getItem().getName().getString(),
//                itemStack.getCount()
//        );
//
//        // Ustaw item w ręce gracza
//        inventory.setStack(selectedSlot, itemStack. copy());
//
//        // Usuń ItemEntity ze świata
//        itemEntity.discard();
//
//        // Zsynchronizuj z klientem
//        syncInventory(player, inventory);
//
//        LOGGER.info("[PICKUP] ✓ SUKCES!");
//        return ActionResult.SUCCESS;
//    }
//
//    /**
//     * Synchronizuje inventory z klientem
//     */
//    private static void syncInventory(net.minecraft.entity.player.PlayerEntity player, PlayerInventory inventory) {
//        if (player instanceof ServerPlayerEntity serverPlayer) {
//            serverPlayer.currentScreenHandler.sendContentUpdates();
//            serverPlayer.playerScreenHandler.onContentChanged(inventory);
//        }
//    }
//}