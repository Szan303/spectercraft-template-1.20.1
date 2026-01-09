// BrickFurnaceEntity.java
package com.szan.block.entity;

import com.szan.block.BrickFurnace;
import com.szan.registry.Block.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BrickFurnaceEntity extends BlockEntity {
    private ItemStack input = ItemStack.EMPTY;
    private ItemStack output = ItemStack.EMPTY;
    private int fuel = 0;
    private int burnTime = 0;

    public BrickFurnaceEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BRICK_FURNACE_ENTITY, pos, state);
    }

    public ActionResult onUse(PlayerEntity player, Hand hand) {
        ItemStack held = player.getStackInHand(hand);

        // Dodaj input jeśli pusty i to "przepalalny" item
        if (input.isEmpty() && !held.isEmpty() && canSmelt(getWorld(), held)) {
            input = held.split(1);
            markDirty();
            return ActionResult.SUCCESS;
        }
        // Dodaj paliwo jeśli jest i masz miejsce
        if (isFuel(held) && fuel < 1200) {
            held.decrement(1);
            fuel += getFuelTime(held); // uniwersalne pobieranie czasu spalania
            markDirty();
            return ActionResult.SUCCESS;
        }
        // Odbiór gotowego produktu
        if (!output.isEmpty()) {
            player.giveItemStack(output.copy());
            output = ItemStack.EMPTY;
            markDirty();
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    // Wywoływane przez ticker w registration
    public static void tick(World world, BlockPos pos, BlockState state, BrickFurnaceEntity entity) {
        if (!entity.input.isEmpty() && entity.fuel > 0 && canSmelt(world, entity.input)) {
            world.setBlockState(pos, state.with(BrickFurnace.LIT, true), 3);
            entity.burnTime++;
            entity.fuel--;
            if (entity.burnTime >= 200) { // czas przepalania, dopasuj do potrzeb
                entity.output = getSmeltingResult(world, entity.input);
                entity.input = ItemStack.EMPTY;
                entity.burnTime = 0;
                entity.markDirty();
            }
        } else {
            world.setBlockState(pos, state.with(BrickFurnace.LIT, false), 3);
        }
    }

    // SPRAWDZANIE: czy można przepalić ten item (world potrzebny do recipes!)
    private static boolean canSmelt(World world, ItemStack stack) {
        if (stack.isEmpty() || world == null) return false;
        SimpleInventory inv = new SimpleInventory(stack.copy());
        return world.getRecipeManager().getFirstMatch(RecipeType.SMELTING, inv, world).isPresent();
    }

    // Wynik przepalania (uwaga: world może być null, w teorii wyłącznie na serwerze)
    private static ItemStack getSmeltingResult(World world, ItemStack stack) {
        if (world == null) return ItemStack.EMPTY;
        SimpleInventory inv = new SimpleInventory(stack.copy());
        return world.getRecipeManager()
                .getFirstMatch(RecipeType.SMELTING, inv, world)
                .map(recipe -> recipe.craft(inv, world.getRegistryManager()))
                .orElse(ItemStack.EMPTY);
    }

    // Sprawdzenie czy stack to fuel - pobiera czas spalania, jak vanilla furnace
    private static boolean isFuel(ItemStack stack) {
        return getFuelTime(stack) > 0;
    }

    // Vanilla fuel time map
    private static int getFuelTime(ItemStack stack) {
        Integer i = net.minecraft.block.entity.AbstractFurnaceBlockEntity.createFuelTimeMap().get(stack.getItem());
        return i == null ? 0 : i;
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.putInt("fuel", fuel);
        tag.putInt("burnTime", burnTime);
        if (!input.isEmpty()) tag.put("input", input.writeNbt(new NbtCompound()));
        if (!output.isEmpty()) tag.put("output", output.writeNbt(new NbtCompound()));
    }
    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        fuel = tag.getInt("fuel");
        burnTime = tag.getInt("burnTime");
        input = tag.contains("input") ? ItemStack.fromNbt(tag.getCompound("input")) : ItemStack.EMPTY;
        output = tag.contains("output") ? ItemStack.fromNbt(tag.getCompound("output")) : ItemStack.EMPTY;
    }
    public int getFuel() {
        return fuel;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public ItemStack getInput() {
        return input;
    }

    public ItemStack getOutput() {
        return output;
    }

}