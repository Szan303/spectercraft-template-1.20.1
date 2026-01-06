package com.szan;

import com.szan.client.ClientNetworking;
import com.szan.client.ItemPickupHandler;
import com. szan.client.KeyBindings;
import com.szan. client.screen.CustomInventoryScreen;
import net.fabricmc.api.ClientModInitializer;
import net. fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client. gui.screen.Screen;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org. slf4j.LoggerFactory;

public class SpecterCraftClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpecterCraft/Client");

    private boolean wasRightPressed = false;

    @Override
    public void onInitializeClient() {
        LOGGER.info("============================================");
        LOGGER.info("[SpecterCraft Client] Inicjalizacja.. .");
        LOGGER.info("============================================");

        // Registry
        KeyBindings.register();
        ClientNetworking.register();

        // Event listeners
        registerTickEvents();

        LOGGER.info("============================================");
        LOGGER.info("[SpecterCraft Client] ✓ Załadowany!");
        LOGGER.info("============================================");
    }

    private void registerTickEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            // === PPM NA ITEMACH (ASYNC) ===
            long window = client.getWindow().getHandle();
            boolean isRightPressed = GLFW.glfwGetMouseButton(window, GLFW. GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

            if (isRightPressed && !wasRightPressed) {
                ItemPickupHandler.handleRightClick(client);
            }

            wasRightPressed = isRightPressed;

            // === INVENTORY TOGGLE (E) ===
            while (KeyBindings. INVENTORY_KEY.wasPressed()) {
                handleInventoryToggle(client);
            }
        });
    }

    private void handleInventoryToggle(MinecraftClient client) {
        if (client.player == null) return;

        Screen currentScreen = client.currentScreen;

        if (currentScreen instanceof CustomInventoryScreen) {
            client.setScreen(null);
            LOGGER.info("[Client] Inventory zamknięty");
        } else if (currentScreen == null) {
            client.setScreen(new CustomInventoryScreen());
            LOGGER.info("[Client] Inventory otwarty");
        }
    }
}