//package com.szan.work;
//
//import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
//import net.minecraft. entity.ItemEntity;
//import net.minecraft. entity.player.PlayerInventory;
//import net.minecraft. item.ItemStack;
//import net.minecraft.util.ActionResult;
//
//public class ManualPickupHandler {
//
//    public static void register() {
//        System.out.println("[DEBUG] ManualPickupHandler. register() called!");
//
//        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
//            System. out.println("[DEBUG] AttackEntityCallback triggered!  Entity: " + entity.getClass().getSimpleName());
//
//            if (world. isClient && entity instanceof ItemEntity itemEntity) {
//                System.out.println("[DEBUG] It's an ItemEntity on client side!");
//
//                PlayerInventory inventory = player.getInventory();
//                int selectedSlot = inventory.selectedSlot;
//                ItemStack handStack = inventory.getStack(selectedSlot);
//
//                System.out.println("[DEBUG] Slot: " + selectedSlot + ", isEmpty: " + handStack.isEmpty());
//
//                if (handStack.isEmpty()) {
//                    ItemStack itemStack = itemEntity.getStack();
//                    inventory.setStack(selectedSlot, itemStack.copy());
//                    itemEntity.discard();
//                    System.out.println("[DEBUG] Item picked up!");
//                    return ActionResult.SUCCESS;
//                } else {
//                    System.out.println("[DEBUG] Hand full, cannot pickup");
//                }
//            }
//            return ActionResult.PASS;
//        });
//    }
//}