package com.szan.registry.recipe;

import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.RecipeType;

public class ModRecipeTypes {
    public static final RecipeType<SmeltingRecipe> BRICK_SMELTING =
            RecipeType.register("spectercraft:brick_smelting");
}

// plik ModRecipeSerializers.java jest niepotrzebny, możesz go skasować albo zostawić pusty