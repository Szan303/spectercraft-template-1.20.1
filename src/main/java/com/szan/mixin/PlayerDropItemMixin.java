package com.szan.mixin;

import com.szan.handler.PlayerDroppedItemTracker;
import net.minecraft.entity.ItemEntity;
import net.minecraft. entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered. asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered. asm.mixin.injection. Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerDropItemMixin {

    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;",
            at = @At("RETURN"))
    private void onItemDropped(ItemStack stack, boolean throwRandomly, boolean retainOwnership,
                               CallbackInfoReturnable<ItemEntity> cir) {
        ItemEntity droppedItem = cir.getReturnValue();

        if (droppedItem != null) {
            // Oznacz jako upuszczony przez gracza
            PlayerDroppedItemTracker.markAsPlayerDropped(droppedItem);
        }
    }
}