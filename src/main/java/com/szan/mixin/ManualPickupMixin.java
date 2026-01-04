package com.szan.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class ManualPickupMixin {
    
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void onInteract(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        System.out.println("============================================");
        System.out.println("[MIXIN DEBUG] interact called (RIGHT CLICK)!");
        System.out.println("[MIXIN DEBUG] Entity: " + entity.getClass().getSimpleName());
        System.out.println("[MIXIN DEBUG] Hand: " + hand);
        System.out.println("[MIXIN DEBUG] Is ItemEntity? " + (entity instanceof ItemEntity));
        System.out.println("============================================");
        
        if (hand == Hand.MAIN_HAND && entity instanceof ItemEntity itemEntity) {
            System.out.println("[PICKUP] Processing ItemEntity with RIGHT CLICK...");
            
            ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
            PlayerInventory inventory = player.getInventory();
            int selectedSlot = inventory.selectedSlot;
            ItemStack handStack = inventory.getStack(selectedSlot);
            
            System.out.println("[PICKUP] Selected slot: " + selectedSlot);
            System.out.println("[PICKUP] Hand empty? " + handStack.isEmpty());
            
            if (handStack.isEmpty()) {
                ItemStack itemStack = itemEntity.getStack();
                System.out.println("[PICKUP] Picking up: " + itemStack.getItem().getName().getString());
                inventory.setStack(selectedSlot, itemStack.copy());
                itemEntity.discard();
                System.out.println("[PICKUP] SUCCESS!");
                cir.setReturnValue(ActionResult.SUCCESS);
            } else {
                System.out.println("[PICKUP] Hand full - cannot pickup");
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}