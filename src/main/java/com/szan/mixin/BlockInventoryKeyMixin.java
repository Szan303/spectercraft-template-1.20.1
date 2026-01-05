package com.szan.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame. InventoryScreen;
import org.spongepowered. asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered. asm.mixin.injection. Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class BlockInventoryKeyMixin {

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void blockInventoryScreen(net.minecraft.client.gui.screen.Screen screen, CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;

        // Blokuj vanilla inventory
        if (screen instanceof InventoryScreen && client.player != null) {
            ci.cancel();
        }
    }
}