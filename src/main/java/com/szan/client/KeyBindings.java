package com.szan.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw. GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyBindings {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpecterCraft/KeyBindings");

    public static KeyBinding INVENTORY_KEY;

    public static void register() {
        LOGGER.info("Rejestrowanie keybindów...");

        INVENTORY_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key. spectercraft.inventory",
                InputUtil.Type.KEYSYM,
                GLFW. GLFW_KEY_E,
                "category.spectercraft"
        ));
    }
}