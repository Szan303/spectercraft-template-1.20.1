package com.szan.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft. entity.player.PlayerInventory;
import net.minecraft.screen. slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org. spongepowered. asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered. asm.mixin.injection. Inject;
import org.spongepowered.asm. mixin.injection.callback.CallbackInfo;
import net.minecraft.screen.ScreenHandler;

@Mixin(ScreenHandler.class)
public class BlockInventorySlotsMixin {

    @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
    private void blockSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        ScreenHandler handler = (ScreenHandler)(Object)this;

        if (slotIndex >= 0 && slotIndex < handler. slots.size()) {
            Slot slot = handler.slots.get(slotIndex);

            if (slot.inventory instanceof PlayerInventory) {
                int index = slot.getIndex();

                // Zablokuj TYLKO sloty 17-35 (te które NIE używasz) oraz 2-8 hotbar
                // Dozwolone:  0 (main hand), 9-16 (inventory), 36-40 (armor + offhand)
                if ((index >= 1 && index <= 8) || (index >= 17 && index <= 35)) {
                    ci. cancel();
                }
            }
        }
    }
}