package com.szan.jade;

import com.szan.block.entity.BrickFurnaceEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.config.IPluginConfig;

import static com.szan.SpecterCraft.MOD_ID;

public enum BrickFurnaceJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getServerData().contains("Fuel")) {
            tooltip.add(Text.literal("Fuel: " + accessor.getServerData().getInt("Fuel")));
        }
    }

    @Override
    public void appendServerData(NbtCompound data, BlockAccessor accessor) { // popraw NBT!
        BrickFurnaceEntity entity = (BrickFurnaceEntity) accessor.getBlockEntity();
        data.putInt("Fuel", entity.getFuel());
    }

    @Override
    public Identifier getUid() { // zamiast ResourceLocation!
        return new Identifier(MOD_ID, "brick_furnace_fuel");
    }
}