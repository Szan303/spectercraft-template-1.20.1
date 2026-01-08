package com.szan.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Debug: zawsze zmniejsz speed (test czy mixin ładuje się)
 */
@Mixin(PlayerEntity.class)
public abstract class SlowMiningMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpecterCraft/SlowMiningMixin");

    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    private void spectercraft_modifyBreakingSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        float original = cir.getReturnValueF();
        float mul = 0.25f; // 25% prędkości - wyraźnie widoczne
        float result = original * mul;
        cir.setReturnValue(result);
        LOGGER.info("[SlowMiningMixin] original={}, result={}", original, result);
    }
}