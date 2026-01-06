package com.szan.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft. client.gui.screen.ingame. HandledScreen;
import net. minecraft.entity.player.PlayerInventory;
import net.minecraft. screen.slot.Slot;
import org.spongepowered. asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered. asm.mixin.injection. Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public class RemoveInventorySlotsMixin {

    @Inject(method = "drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void hideInventorySlots(DrawContext context, Slot slot, CallbackInfo ci) {
        if (slot.inventory instanceof PlayerInventory) {
            int index = slot.getIndex();

            // Ukryj TYLKO sloty 17-35 (te które NIE używasz)
            // Pokazuj:  0 (main hand), 9-16 (inventory), 36-40 (armor + offhand)
            if ((index >= 1 && index <= 8) || (index >= 17 && index <= 35)) {
                ci.cancel();
            }
        }
    }
}