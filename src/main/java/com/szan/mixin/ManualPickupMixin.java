package com.szan.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft. item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit. HitResult;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin. Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered. asm.mixin.injection. Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class ManualPickupMixin {

    @Shadow public ClientPlayerEntity player;
    @Shadow public HitResult crosshairTarget;

    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void onHandleInput(CallbackInfo ci) {
        MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();

        // Sprawdź czy gracz kliknął PPM
        if (client.options.useKey. isPressed() && crosshairTarget != null) {
            if (crosshairTarget.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHit = (EntityHitResult) crosshairTarget;
                Entity entity = entityHit.getEntity();

                if (entity instanceof ItemEntity itemEntity && player != null) {
                    PlayerInventory inventory = player.getInventory();
                    int selectedSlot = inventory.selectedSlot;
                    ItemStack handStack = inventory.getStack(selectedSlot);

                    // Jeśli ręka jest pusta, podnieś item
                    if (handStack. isEmpty()) {
                        ItemStack itemStack = itemEntity.getStack();
                        inventory.setStack(selectedSlot, itemStack. copy());
                        itemEntity. discard();
                    }
                }
            }
        }
    }
}