package com.szan.mixin;

import net.minecraft. client.MinecraftClient;
import net.minecraft.client. network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm. mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm. mixin.injection.Inject;
import org.spongepowered.asm.mixin. injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class BlockInventoryMixin {

    @Inject(method = "closeHandledScreen", at = @At("HEAD"))
    private void onCloseScreen(CallbackInfo ci) {
        // Nic nie rób, tylko dla referencji
    }
}