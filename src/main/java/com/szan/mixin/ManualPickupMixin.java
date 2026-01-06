package com.szan.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft. entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft. item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft. util.Hand;
import org.spongepowered. asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm. mixin.injection.Inject;
import org.spongepowered.asm.mixin. injection.callback.CallbackInfoReturnable;

@Mixin(value = ClientPlayerInteractionManager.class, priority = 1500)
public class ManualPickupMixin {

    @Inject(method = "interactEntity", at = @At("HEAD"), cancellable = true)
    private void onInteractEntity(PlayerEntity player, Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        System.out.println("============================================");
        System.out. println("[MIXIN DEBUG] ManualPickupMixin - interactEntity!");
        System.out.println("[MIXIN DEBUG] Entity: " + entity.getClass().getSimpleName());
        System.out.println("[MIXIN DEBUG] Hand: " + hand);
        System.out.println("============================================");

        if (hand == Hand.MAIN_HAND && entity instanceof ItemEntity itemEntity) {
            System.out.println("[PICKUP] ItemEntity detected with RIGHT CLICK!");

            PlayerInventory inventory = player.getInventory();
            int selectedSlot = inventory.selectedSlot;
            ItemStack handStack = inventory.getStack(selectedSlot);

            System.out.println("[PICKUP] Slot: " + selectedSlot + ", isEmpty: " + handStack.isEmpty());

            if (handStack.isEmpty()) {
                ItemStack itemStack = itemEntity.getStack();
                System.out.println("[PICKUP] Picking up:  " + itemStack.getItem().getName().getString());
                inventory.setStack(selectedSlot, itemStack. copy());
                itemEntity.discard();

                // BRAK COOLDOWNU!  Item znika = blokada znika natychmiast

                System.out.println("[PICKUP] SUCCESS!");
                cir.setReturnValue(ActionResult.SUCCESS);
            } else {
                System.out.println("[PICKUP] Hand full - cannot pickup");
                cir.setReturnValue(ActionResult. FAIL);
            }
        }
    }
}