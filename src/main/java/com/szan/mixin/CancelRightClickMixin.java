//package com.szan.mixin;
//
//import com.szan.handler.RightClickCanceller;
//import net.minecraft.client. network.ClientPlayerEntity;
//import net.minecraft.client.network. ClientPlayerInteractionManager;
//import net.minecraft.entity.player.PlayerEntity;  // ← DODAJ TEŻ TEN!
//import net.minecraft.util.ActionResult;
//import net. minecraft.util.Hand;
//import net.minecraft.util.hit. BlockHitResult;
//import org.spongepowered. asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered. asm.mixin.injection. Inject;
//import org.spongepowered.asm. mixin.injection.callback.CallbackInfoReturnable;
//import org.slf4j.Logger;
//import org.slf4j. LoggerFactory;
//
//@Mixin(ClientPlayerInteractionManager. class)
//public class CancelRightClickMixin {
//    private static final Logger LOGGER = LoggerFactory.getLogger("SpecterCraft/Mixin/RightClick");
//
//    @Inject(
//            method = "interactBlock",
//            at = @At("HEAD"),
//            cancellable = true
//    )
//    private void onInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
//        if (RightClickCanceller.shouldCancel()) {
//            LOGGER.info("[Mixin] ✗ Anulowano interactBlock (po pickup)");
//            cir.setReturnValue(ActionResult.PASS);
//            cir.cancel();
//        }
//    }
//
//    @Inject(
//            method = "interactItem",
//            at = @At("HEAD"),
//            cancellable = true
//    )
//    private void onInteractItem(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {  // ← PlayerEntity tutaj!
//        if (RightClickCanceller.shouldCancel()) {
//            LOGGER.info("[Mixin] ✗ Anulowano interactItem (po pickup)");
//            cir.setReturnValue(ActionResult.PASS);
//            cir.cancel();
//        }
//    }
//}