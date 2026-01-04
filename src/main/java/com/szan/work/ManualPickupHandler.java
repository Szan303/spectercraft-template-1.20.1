package com.szan.work;

import net.fabricmc.fabric. api.event.player.AttackEntityCallback;
import net. minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft. util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ManualPickupHandler {

    public static void register() {
        AttackEntityCallback. EVENT.register((player, world, hand, entity, hitResult) -> {
            // Tylko client-side
            if (world.isClient && entity instanceof ItemEntity itemEntity) {
                System.out.println("It's an ItemEntity!");
                PlayerInventory inventory = player. getInventory();
                int selectedSlot = inventory.selectedSlot;
                ItemStack handStack = inventory.getStack(selectedSlot);

                // Jeśli ręka jest pusta, podnieś item
                if (handStack.isEmpty()) {
                    ItemStack itemStack = itemEntity.getStack();
                    inventory.setStack(selectedSlot, itemStack.copy());
                    itemEntity.discard();
                    return ActionResult.SUCCESS;
                }
                return ActionResult.FAIL; // Nie pozwól zniszczyć itemu jeśli ręka pełna
            }
            return ActionResult.PASS;
        });
    }
}