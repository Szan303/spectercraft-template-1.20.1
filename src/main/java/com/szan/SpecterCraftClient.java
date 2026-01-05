package com.szan;

import com.szan.client.CustomInventoryScreen;
import com.szan. client.RecipeSelectionScreen;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api. client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api. client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client. networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event. player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net. minecraft.client.util.InputUtil;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util. ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util. TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net. minecraft.util.math.Box;
import net.minecraft.util. math.Vec3d;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SpecterCraftClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory. getLogger("SpecterCraft/Client");

    private boolean wasRightPressed = false;
    private boolean shouldCancelUse = false;

    // Key binding dla inventory toggle
    private static KeyBinding inventoryKeyBinding;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[SpecterCraft Client] Inicjalizacja.. .");

        // Zarejestruj custom key binding dla inventory
        inventoryKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key. spectercraft.inventory",
                InputUtil.Type.KEYSYM,
                GLFW. GLFW_KEY_E,
                "category.spectercraft"
        ));

        // Tick event (TYLKO dla inventory toggle)
        ClientTickEvents. END_CLIENT_TICK.register(client -> {
            if (client. player == null || client.world == null) {
                return;
            }

            // Sprawdź inventory key (wasPressed = jeden raz na klik)
            while (inventoryKeyBinding.wasPressed()) {
                onInventoryKeyPressed(client);
            }
        });

        // UseBlock - TUTAJ obsługuj prawy klik na itemach!
        UseBlockCallback.EVENT. register((player, world, hand, hitResult) -> {
            if (shouldCancelUse) {
                shouldCancelUse = false;
                return ActionResult. FAIL;
            }

            // Sprawdź czy kliknął na ItemEntity
            if (hand == Hand.MAIN_HAND && ! Screen.hasShiftDown()) {
                return ActionResult.PASS; // Normalne użycie bloku
            }

            return ActionResult.PASS;
        });

        // UseItem - obsługuj prawy klik w powietrzu
        UseItemCallback. EVENT.register((player, world, hand) -> {
            if (shouldCancelUse) {
                shouldCancelUse = false;
                return TypedActionResult.fail(player.getStackInHand(hand));
            }

            // Sprawdź czy patrzy na ItemEntity
            if (hand == Hand.MAIN_HAND) {
                var client = net.minecraft.client.MinecraftClient.getInstance();
                EntityHitResult hit = raycastItemEntity(client);

                if (hit != null && hit.getEntity() instanceof ItemEntity itemEntity) {
                    boolean isCrafting = Screen.hasShiftDown();

                    LOGGER.info("[Client] Klik na ItemEntity | Crafting: {}", isCrafting);

                    PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                    buf.writeInt(itemEntity.getId());
                    buf. writeBoolean(isCrafting);
                    ClientPlayNetworking.send(SpecterCraft.PICKUP_PACKET_ID, buf);

                    player.swingHand(Hand. MAIN_HAND);

                    return TypedActionResult. success(player.getStackInHand(hand), true);
                }
            }

            return TypedActionResult.pass(player.getStackInHand(hand));
        });

        // Rejestracja packetu RECIPE_LIST
        ClientPlayNetworking. registerGlobalReceiver(
                SpecterCraft.RECIPE_LIST_PACKET_ID,
                (client, handler, buf, responseSender) -> {
                    int count = buf.readInt();
                    List<RecipeSelectionScreen.RecipeEntry> recipes = new ArrayList<>();

                    for (int i = 0; i < count; i++) {
                        Identifier recipeId = buf.readIdentifier();
                        ItemStack result = buf.readItemStack();
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
                    }

                    client.execute(() -> {
                        client.setScreen(new RecipeSelectionScreen(recipes));
                    });
                }
        );

        LOGGER.info("[SpecterCraft Client] ✓ Załadowany!");
    }

    /**
     * Raycast do ItemEntity
     */
    private EntityHitResult raycastItemEntity(net.minecraft.client.MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) {
            return null;
        }

        double reach = client.interactionManager.getReachDistance();
        Vec3d cameraPos = client.player.getCameraPosVec(1.0F);
        Vec3d lookVec = client.player.getRotationVec(1.0F);
        Vec3d reachEnd = cameraPos.add(lookVec.multiply(reach));

        Box box = client.player.getBoundingBox()
                .stretch(lookVec.multiply(reach))
                .expand(1.0);

        return ProjectileUtil.raycast(
                client.player,
                cameraPos,
                reachEnd,
                box,
                entity -> entity instanceof ItemEntity,
                reach * reach
        );
    }

    /**
     * Toggle inventory (E key)
     */
    private void onInventoryKeyPressed(net.minecraft.client. MinecraftClient client) {
        Screen currentScreen = client.currentScreen;

        if (currentScreen instanceof CustomInventoryScreen) {
            // Zamknij inventory
            client.setScreen(null);
            LOGGER.info("[Client] Inventory zamknięty");
        } else if (currentScreen == null) {
            // Otwórz inventory
            client.setScreen(new CustomInventoryScreen());
            LOGGER.info("[Client] Inventory otwarty");
        }
    }
}