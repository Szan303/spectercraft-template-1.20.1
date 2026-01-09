package com.szan.block.entity;

import com.szan.registry.block.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.state.property.Properties;
import net.minecraft.world.World;

public class BrickFurnaceEntity extends AbstractFurnaceBlockEntity {
    static int cookSlownessMultiplier = 3;

    public BrickFurnaceEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BRICK_FURNACE_ENTITY, pos, state, RecipeType.SMELTING);
    }

    @Override
    protected Text getContainerName() {
        return Text.translatable("container.spectercraft.brick_furnace");
    }

    @Override
    public ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new FurnaceScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    private static boolean canAcceptRecipeOutput(Inventory inv, SmeltingRecipe recipe, World world) {
        if (recipe == null) return false;

        ItemStack input = inv.getStack(0);
        ItemStack result = recipe.getOutput(world.getRegistryManager());
        ItemStack outputSlot = inv.getStack(2);

        if (input.isEmpty()) return false;
        if (result.isEmpty()) return false;
        if (outputSlot.isEmpty()) return true;
        if (!ItemStack.canCombine(outputSlot, result)) return false;
        return outputSlot.getCount() + result.getCount() <= outputSlot.getMaxCount();
    }

    private static void craftRecipe(Inventory inv, SmeltingRecipe recipe, World world) {
        if (recipe == null) return;

        ItemStack input = inv.getStack(0);
        ItemStack result = recipe.getOutput(world.getRegistryManager()).copy();
        ItemStack outputSlot = inv.getStack(2);

        if (outputSlot.isEmpty()) {
            inv.setStack(2, result);
        } else if (ItemStack.canCombine(outputSlot, result)) {
            outputSlot.increment(result.getCount());
            inv.setStack(2, outputSlot);
        }
        input.decrement(1);
        inv.setStack(0, input);
    }

    public static void tick(World world, BlockPos pos, BlockState state, BrickFurnaceEntity furnace) {
        Inventory inv = furnace; // AbstractFurnaceBlockEntity implementuje Inventory

        boolean isLit = state.get(Properties.LIT);

        ItemStack fuelStack = inv.getStack(1);
        boolean hasFuel = !fuelStack.isEmpty();

        SmeltingRecipe recipe = world.getRecipeManager()
                .getFirstMatch(RecipeType.SMELTING, furnace, world).orElse(null);

        boolean canSmelt = canAcceptRecipeOutput(inv, recipe, world);

        if ((isLit || hasFuel) && canSmelt) {
            int cookTime = furnace.propertyDelegate.get(2);
            int cookTimeTotal = (recipe != null) ? recipe.getCookTime() : 200;

            if (world.getTime() % cookSlownessMultiplier == 0) {
                furnace.propertyDelegate.set(2, cookTime + 1);
            }

            cookTime = furnace.propertyDelegate.get(2);
            if (cookTime >= cookTimeTotal) {
                furnace.propertyDelegate.set(2, 0);
                craftRecipe(inv, recipe, world);
                furnace.markDirty();
            }
        } else {
            furnace.propertyDelegate.set(2, 0);
        }
    }
}