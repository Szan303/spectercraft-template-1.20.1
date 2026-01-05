package com.szan;

import com.szan.client.RecipeSelectionScreen;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net. fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client. networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.ItemEntity;
import net.minecraft. entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net. minecraft.network.PacketByteBuf;
import net.minecraft.util.ActionResult;
import net.minecraft. util.Hand;
import net. minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net. minecraft.util.hit.EntityHitResult;
import net. minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j. LoggerFactory;

import java. util.ArrayList;
import java. util.List;

public class SpecterCraftClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpecterCraft/Client");

    private boolean wasRightPressed = false;
    private boolean shouldCancelUse = false;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[SpecterCraft Client] Inicjalizacja.. .");

        // Sprawdzaj prawy przycisk myszy co tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) {
                return;
            }

            long window = client.getWindow().getHandle();
            boolean isRightPressed = GLFW.glfwGetMouseButton(window, GLFW. GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

            if (isRightPressed && !wasRightPressed) {
                onRightClick(client);
            }

            wasRightPressed = isRightPressed;
        });

        // Anuluj użycie bloku
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (shouldCancelUse) {
                shouldCancelUse = false;
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        // Anuluj użycie itemu
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (shouldCancelUse) {
                shouldCancelUse = false;
                return TypedActionResult.fail(player.getStackInHand(hand));
            }
            return TypedActionResult.pass(player. getStackInHand(hand));
        });

        // Rejestracja packetu RECIPE_LIST (serwer → klient)
        ClientPlayNetworking.registerGlobalReceiver(
                SpecterCraft.RECIPE_LIST_PACKET_ID,
                (client, handler, buf, responseSender) -> {
                    // Odczytaj receptury z packetu
                    int count = buf.readInt();
                    List<RecipeSelectionScreen.RecipeEntry> recipes = new ArrayList<>();

                    LOGGER.info("[Client] Otrzymano {} receptur z serwera", count);

                    for (int i = 0; i < count; i++) {
                        Identifier recipeId = buf.readIdentifier();
                        ItemStack result = buf. readItemStack();
                        int totalCount = buf.readInt();

                        int ingredientCount = buf.readInt();
                        List<ItemStack> ingredients = new ArrayList<>();
                        for (int j = 0; j < ingredientCount; j++) {
                            ingredients.add(buf.readItemStack());
                        }

                        recipes.add(new RecipeSelectionScreen.RecipeEntry(
                                recipeId,
                                result,
                                totalCount,
                                ingredients
                        ));

                        LOGGER.debug("[Client] Receptura #{}:  {} x{}", i + 1, result.getName().getString(), totalCount);
                    }

                    // Otwórz GUI na main thread
                    client.execute(() -> {
                        client.setScreen(new RecipeSelectionScreen(recipes));
                        LOGGER.info("[Client] Otwarto RecipeSelectionScreen");
                    });
                }
        );

        LOGGER.info("[SpecterCraft Client] ✓ Załadowany!");
    }

    private void onRightClick(net.minecraft.client.MinecraftClient client) {
        assert client.interactionManager != null;

        double reach = client.interactionManager.getReachDistance();
        Vec3d cameraPos = client.player.getCameraPosVec(1.0F);
        Vec3d lookVec = client.player.getRotationVec(1.0F);
        Vec3d reachEnd = cameraPos.add(lookVec.multiply(reach));

        Box box = client.player.getBoundingBox()
                .stretch(lookVec.multiply(reach))
                .expand(1.0);

        EntityHitResult hit = ProjectileUtil.raycast(
                client.player,
                cameraPos,
                reachEnd,
                box,
                entity -> entity instanceof ItemEntity,
                reach * reach
        );

        if (hit != null && hit.getEntity() instanceof ItemEntity itemEntity) {
            boolean isCrafting = Screen.hasShiftDown();

            LOGGER.info("[Client] Klik na ItemEntity | Crafting: {}", isCrafting);

            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeInt(itemEntity.getId());
            buf.writeBoolean(isCrafting);
            ClientPlayNetworking.send(SpecterCraft.PICKUP_PACKET_ID, buf);

            client.player.swingHand(Hand.MAIN_HAND);
            shouldCancelUse = true;
        }
    }
}