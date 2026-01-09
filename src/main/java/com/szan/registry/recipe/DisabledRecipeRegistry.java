package com.szan.registry.recipe;

import net.minecraft.util.Identifier;

import java.util.Set;

public class DisabledRecipeRegistry {
    // Dodaj tutaj identyfikatory itemów, które chcesz zablokować w przepisach.
    private static final Set<Identifier> HARDCODED_DISABLED = Set.of(
            new Identifier("minecraft", "stick"),
            new Identifier("minecraft", "crafting_table")
            // Dodaj kolejne: new Identifier("namespace", "id"),
    );

    // Sprawdza, czy dany item (id typu "namespace:item") powinien być zablokowany
    public static boolean isRecipeDisabled(String itemId) {
        Identifier id = Identifier.tryParse(itemId);
        return id != null && HARDCODED_DISABLED.contains(id);
    }
}