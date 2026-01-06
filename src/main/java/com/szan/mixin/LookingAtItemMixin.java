package com.szan.mixin;

import com.szan. handler.RightClickCanceller;
import net.minecraft.client.MinecraftClient;
import net. minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft. util.hit.EntityHitResult;
import net.minecraft.util.hit. HitResult;
import org. spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin. Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm. mixin.injection.Inject;
import org.spongepowered.asm.mixin. injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class LookingAtItemMixin {

    @Shadow
    private MinecraftClient client;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (client != null && client.crosshairTarget != null) {
            HitResult target = client.crosshairTarget;

            // Sprawdź czy patrzysz na ItemEntity
            if (target. getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHit = (EntityHitResult) target;

                if (entityHit.getEntity() instanceof ItemEntity) {
                    // PATRZYSZ NA ITEM - ZABLOKUJ PPM
                    RightClickCanceller.setLookingAtItem(true);
                    return;
                }
            }
        }

        // NIE PATRZYSZ NA ITEM - ODBLOKUJ
        RightClickCanceller. setLookingAtItem(false);
    }
}