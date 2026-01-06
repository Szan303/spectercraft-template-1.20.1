package com.szan.mixin;

import com.szan.client.screen.CustomInventoryScreen;
import net.minecraft.client. MinecraftClient;
import net. minecraft.client.gui.screen. ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered. asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered. asm.mixin.injection. Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInventoryOpen(PlayerEntity player, CallbackInfo ci) {
        // Gdy vanilla inventory się otwiera, zamknij go i otwórz custom
        MinecraftClient client = MinecraftClient.getInstance();

        client.execute(() -> {
            client.setScreen(new CustomInventoryScreen());
        });
    }
}