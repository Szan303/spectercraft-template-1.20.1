package com.szan.block.entity;

import com.szan.registry.block.ModBlockEntities;
import com.szan.registry.recipe.ModRecipeTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;

public class BrickFurnaceEntity extends AbstractFurnaceBlockEntity {
    public BrickFurnaceEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BRICK_FURNACE_ENTITY, pos, state, ModRecipeTypes.BRICK_SMELTING);
    }

    @Override
    protected Text getContainerName() {
        return Text.translatable("container.spectercraft.brick_furnace");
    }

    @Override
    public ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new FurnaceScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }
}