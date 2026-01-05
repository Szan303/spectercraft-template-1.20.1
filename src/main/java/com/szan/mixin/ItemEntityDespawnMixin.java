package com.szan.mixin;

import com.szan.handler. PlayerDroppedItemTracker;
import net.minecraft.entity. ItemEntity;
import org.spongepowered. asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered. asm.mixin.injection. Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityDespawnMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void preventPlayerDroppedDespawn(CallbackInfo ci) {
        ItemEntity itemEntity = (ItemEntity) (Object) this;

        // Sprawdź czy item został upuszczony przez gracza
        if (PlayerDroppedItemTracker.isPlayerDropped(itemEntity)) {
            // Resetuj age żeby nigdy nie despawnował
            itemEntity.age = 0;
        }
    }
}