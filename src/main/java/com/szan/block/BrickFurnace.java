package com.szan.block;

import com.szan.block.entity.BrickFurnaceEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class BrickFurnace extends FurnaceBlock {
    public BrickFurnace(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BrickFurnaceEntity(pos, state);
    }
}
