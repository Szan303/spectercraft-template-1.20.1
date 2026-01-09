package com.szan.registry.recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmeltingRecipe;

public class ModRecipeSerializers {
    private static boolean registered = false;

    public static RecipeSerializer<SmeltingRecipe> BRICK_SMELTING;

    public static void register() {
        if (!registered) {
            BRICK_SMELTING = RecipeSerializer.register("spectercraft:brick_smelting", RecipeSerializer.SMELTING);
            registered = true;
        }
    }

    private ModRecipeSerializers() {} // zapobiega stworzeniu instancji klasy
}