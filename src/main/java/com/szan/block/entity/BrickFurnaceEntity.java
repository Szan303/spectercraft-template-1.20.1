package com.szan.block.entity;

import com.szan.registry.Block.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import com.szan.registry.Block.ModBlockEntities;

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

        // Dodaj input jeśli pusty i to "przepalalny" item (tu uprość, później rozwiń)
        if (input.isEmpty() && !held.isEmpty() && canSmelt(held)) {
            input = held.split(1);
            markDirty();
            return ActionResult.SUCCESS;
        }
        // Dodaj paliwo jeśli jest i masz miejsce
        if (isFuel(held) && fuel < 1200) {
            held.decrement(1);
            fuel += 200; // coal etc
            markDirty();
            return ActionResult.SUCCESS;
        }
        // Odbiór gotowego produktu
        if (output != ItemStack.EMPTY && !output.isEmpty()) {
            player.giveItemStack(output.copy());
            output = ItemStack.EMPTY;
            markDirty();
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    // Tick — wywołuj z Ticker
    public static void tick(World world, BlockPos pos, BlockState state, BrickFurnaceEntity entity) {
        if (!entity.input.isEmpty() && entity.fuel > 0) {
            entity.burnTime++;
            entity.fuel--;
            if (entity.burnTime >= 200) { // czas przepalania
                entity.output = new ItemStack(getSmeltingResult(entity.input).getItem());
                entity.input = ItemStack.EMPTY;
                entity.burnTime = 0;
                entity.markDirty();
            }
        }
    }

    private static boolean canSmelt(ItemStack stack) {
        // Możesz sprawdzić Recipes tu! (skrócone)
        return !stack.isEmpty(); // TODO: zamień na sprawdzanie "czy item jest przepalalny"
    }

    private static boolean isFuel(ItemStack stack) {
        return stack.getItem() == Items.COAL || stack.getItem() == Items.CHARCOAL;
    }

    private static ItemStack getSmeltingResult(ItemStack input) {
        // TODO: użyj RecipeManager, teraz testowo bread dla mięsa
        if (input.getItem() == Items.BEEF) return new ItemStack(Items.COOKED_BEEF);
        if (input.getItem() == Items.PORKCHOP) return new ItemStack(Items.COOKED_PORKCHOP);
        return ItemStack.EMPTY;
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
}