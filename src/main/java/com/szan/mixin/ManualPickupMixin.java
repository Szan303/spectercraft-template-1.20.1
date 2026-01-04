package com.szan.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft. entity. player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered. asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered. asm.mixin.injection. Inject;
import org.spongepowered.asm. mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class ManualPickupMixin {

    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        System.out.println("============================================");
        System.out. println("[MIXIN DEBUG] attackEntity called!");
        System.out.println("[MIXIN DEBUG] Target: " + target.getClass().getSimpleName());
        System.out.println("[MIXIN DEBUG] Is ItemEntity? " + (target instanceof ItemEntity));
        System.out.println("============================================");

        if (target instanceof ItemEntity itemEntity) {
            System.out.println("[PICKUP] Processing ItemEntity...");

            PlayerInventory inventory = player.getInventory();
            int selectedSlot = inventory.selectedSlot;
            ItemStack handStack = inventory.getStack(selectedSlot);

            System.out. println("[PICKUP] Selected slot: " + selectedSlot);
            System.out.println("[PICKUP] Hand empty?  " + handStack.isEmpty());

            if (handStack.isEmpty()) {
                ItemStack itemStack = itemEntity.getStack();
                System.out.println("[PICKUP] Picking up:  " + itemStack.getItem().getName().getString());
                inventory.setStack(selectedSlot, itemStack. copy());
                itemEntity.discard();
                System.out. println("[PICKUP] SUCCESS!");
            } else {
                System.out.println("[PICKUP] Hand full - cannot pickup");
            }

            ci.cancel();
        }
    }
}