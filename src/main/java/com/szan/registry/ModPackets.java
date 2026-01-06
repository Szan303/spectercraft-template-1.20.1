package com.szan.registry;

import com.szan.SpecterCraft;
import net.minecraft.util.Identifier;

public class ModPackets {
    // ========== PACKET IDS ==========
    public static final Identifier PICKUP = new Identifier(SpecterCraft.MOD_ID, "pickup");
    public static final Identifier RECIPE_LIST = new Identifier(SpecterCraft.MOD_ID, "recipe_list");
    public static final Identifier RECIPE_SELECTED = new Identifier(SpecterCraft.MOD_ID, "recipe_selected");
    public static final Identifier SLOT_SWAP = new Identifier(SpecterCraft.MOD_ID, "slot_swap");
    public static final Identifier SLOT_CLICK = new Identifier(SpecterCraft.MOD_ID, "slot_click");

    public static void register() {
        // Nie trzeba nic robić - same identifiers
    }
}