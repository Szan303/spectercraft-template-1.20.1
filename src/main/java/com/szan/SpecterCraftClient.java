package com.szan;

import com.szan.client.CustomInventoryScreen;
import com.szan.client.RecipeSelectionScreen;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client. networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net. minecraft.client.gui.screen. Screen;
import net.minecraft. client.option.KeyBinding;
import net.minecraft. client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft. item.ItemStack;
import net.minecraft. network.PacketByteBuf;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org. slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent. CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util. concurrent.Executors;

public class SpecterCraftClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpecterCraft/Client");

    private boolean wasRightPressed = false;
    private static KeyBinding inventoryKeyBinding;

    // Thread pool dla async operacji
    private static final ExecutorService ASYNC_EXECUTOR = Executors.newFixedThreadPool(2);

    @Override
    public void onInitializeClient() {
        LOGGER.info("============================================");
        LOGGER.info("[SpecterCraft Client] Inicjalizacja.. .");
        LOGGER.info("============================================");

        // Zarejestruj key binding dla inventory
        inventoryKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.spectercraft.inventory",
                InputUtil.Type.KEYSYM,
                GLFW. GLFW_KEY_E,
                "category.spectercraft"
        ));

        // Tick event - PPM + inventory toggle
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) {
                return;
            }

            // === PPM NA ITEMACH (ASYNC) ===
            long window = client.getWindow().getHandle();
            boolean isRightPressed = GLFW.glfwGetMouseButton(window, GLFW. GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

            if (isRightPressed && !wasRightPressed) {
                handleRightClickAsync(client);
            }

            wasRightPressed = isRightPressed;

            // === INVENTORY TOGGLE (E) ===
            while (inventoryKeyBinding.wasPressed()) {
                handleInventoryToggle(client);
            }
        });

        // Rejestracja packetu RECIPE_LIST
        ClientPlayNetworking. registerGlobalReceiver(
                SpecterCraft. RECIPE_LIST_PACKET_ID,
                (client, handler, buf, responseSender) -> {
                    int count = buf.readInt();
                    List<RecipeSelectionScreen.RecipeEntry> recipes = new ArrayList<>();

                    LOGGER.info("[Client] Otrzymano {} receptur z serwera", count);

                    for (int i = 0; i < count; i++) {
                        Identifier recipeId = buf.readIdentifier();
                        ItemStack result = buf.readItemStack();
                        int totalCount = buf.readInt();

                        int ingredientCount = buf.readInt();
                        List<ItemStack> ingredients = new ArrayList<>();
                        for (int j = 0; j < ingredientCount; j++) {
                            ingredients. add(buf.readItemStack());
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
                        LOGGER.info("[Client] Otwarto RecipeSelectionScreen");
                    });
                }
        );

        LOGGER.info("============================================");
        LOGGER.info("[SpecterCraft Client] ✓ Załadowany!");
        LOGGER.info("============================================");
    }

    /**
     * Obsługa PPM (ASYNC - nie blokuje gry)
     */
    private void handleRightClickAsync(MinecraftClient client) {
        // Skopiuj dane potrzebne do obliczeń (żeby wątek miał snapshot)
        if (client.player == null || client.world == null) {
            return;
        }

        Vec3d cameraPos = client.player.getCameraPosVec(1.0F);
        Vec3d lookVec = client.player.getRotationVec(1.0F);
        double maxReach = 4.5;

        // Skopiuj listę itemów (żeby wątek nie dotykał client.world)
        List<ItemEntity> nearbyItems = new ArrayList<>(client.world.getEntitiesByClass(
                ItemEntity.class,
                client.player.getBoundingBox().expand(maxReach),
                entity -> ! entity.isRemoved()
        ));

        LOGGER.debug("[Client Async] Znaleziono {} itemów do przetworzenia", nearbyItems.size());

        // ASYNC: Znajdź najbliższy item (w tle)
        CompletableFuture. supplyAsync(() -> {
            return findNearestItemEntityAsync(nearbyItems, cameraPos, lookVec, maxReach);
        }, ASYNC_EXECUTOR).thenAcceptAsync(target -> {
            // SYNC: Wyślij packet (musi być w main thread)
            client.execute(() -> {
                handleItemFound(client, target);
            });
        });
    }

    /**
     * ASYNC: Znajdź najbliższy ItemEntity (nie blokuje gry)
     */
    private ItemEntity findNearestItemEntityAsync(List<ItemEntity> items, Vec3d cameraPos, Vec3d lookVec, double maxReach) {
        long startTime = System.nanoTime();

        ItemEntity closest = null;
        double closestDistance = maxReach;

        for (ItemEntity item :  items) {
            Vec3d itemPos = item.getPos().add(0, item.getHeight() / 2, 0);
            Vec3d toItem = itemPos.subtract(cameraPos);
            double distance = toItem.length();

            if (distance > maxReach) {
                continue;
            }

            Vec3d toItemNormalized = toItem.normalize();
            double dotProduct = lookVec.dotProduct(toItemNormalized);

            if (dotProduct > 0.95 && distance < closestDistance) {
                closest = item;
                closestDistance = distance;
            }
        }

        long duration = System.nanoTime() - startTime;
        LOGGER. debug("[Client Async] Raycast zajął {} μs", duration / 1000);

        return closest;
    }

    /**
     * SYNC: Obsłuż znaleziony item (main thread)
     */
    private void handleItemFound(MinecraftClient client, ItemEntity target) {
        LOGGER.info("========================================");
        LOGGER.info("[Client] Wynik async raycast");
        LOGGER.info("[Client] Znaleziony ItemEntity: {}", target);

        if (target != null && ! target.isRemoved()) {
            boolean isCrafting = Screen.hasShiftDown();

            LOGGER.info("[Client] ✓ Target to ItemEntity!");
            LOGGER.info("[Client] Crafting: {}", isCrafting);
            LOGGER.info("[Client] EntityID:  {}", target.getId());
            LOGGER.info("[Client] Stack: {}", target.getStack());

            // Wyślij packet do serwera (MUSI być main thread)
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeInt(target.getId());
            buf.writeBoolean(isCrafting);
            ClientPlayNetworking.send(SpecterCraft.PICKUP_PACKET_ID, buf);

            client.player.swingHand(Hand.MAIN_HAND);

            LOGGER.info("[Client] ✓ Packet wysłany!");
        } else {
            LOGGER.info("[Client] ✗ Brak ItemEntity w zasięgu wzroku");
        }

        LOGGER.info("========================================");
    }

    /**
     * Toggle inventory (E key)
     */
    private void handleInventoryToggle(MinecraftClient client) {
        Screen currentScreen = client.currentScreen;

        if (currentScreen instanceof CustomInventoryScreen) {
            client.setScreen(null);
            LOGGER.info("[Client] Inventory zamknięty (toggle)");
        } else if (currentScreen == null) {
            client.setScreen(new CustomInventoryScreen());
            LOGGER.info("[Client] Inventory otwarty (toggle)");
        }
    }
}