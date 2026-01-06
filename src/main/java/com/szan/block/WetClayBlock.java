package com.szan.block;

import com. szan.block.entity.WetClayBlockEntity;
import com.szan.registry.ModBlockEntities;
import net.minecraft. block.Block;
import net. minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft. block.entity.BlockEntity;
import net. minecraft.block.entity.BlockEntityTicker;
import net.minecraft. block.entity.BlockEntityType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft. util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WetClayBlock extends BlockWithEntity {
    // Etapy wysychania:  0 = mokra, 1-3 = wysychanie, 4 = sucha (zamienia się w brick)
    public static final IntProperty DRYING_STAGE = IntProperty. of("drying_stage", 0, 4);

    public WetClayBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(DRYING_STAGE, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(DRYING_STAGE);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new WetClayBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : checkType(type, ModBlockEntities.WET_CLAY_ENTITY, WetClayBlockEntity:: tick);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
}