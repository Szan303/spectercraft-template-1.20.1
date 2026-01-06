package com.szan.registry;

import com.szan. SpecterCraft;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util. Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModItems {
    private static final Logger LOGGER = LoggerFactory. getLogger("SpecterCraft/Registry/Items");

    // ========== ITEM DEFINITIONS ==========

    // Przykład custom item:
    // public static final Item CUSTOM_ITEM = registerItem("custom_item",
    //     new Item(new Item.Settings())
    // );

    // Dodaj więcej itemów tutaj...

    // ========== HELPER METHODS ==========

    /**
     * Rejestruje item
     */
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries. ITEM, new Identifier(SpecterCraft.MOD_ID, name), item);
    }

    /**
     * Wywołaj to w onInitialize()
     */
    public static void register() {
        LOGGER.info("Rejestrowanie itemów...");
        // Itemy są rejestrowane przy inicjalizacji static fields
    }
}