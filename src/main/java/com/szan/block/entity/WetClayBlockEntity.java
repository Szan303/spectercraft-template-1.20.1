package com.szan.block.entity;

import com.szan.block.WetClayBlock;
import com.szan.registry.Block.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * BlockEntity odpowiedzialny za zwiększanie drying stage co pewien czas.
 */
public class WetClayBlockEntity extends BlockEntity {
    private static final int STAGE_TICKS = (100 * 12) * 20; // ticks między kolejnymi stage (np. 100 ~ 5s)
    private int tickCounter = 0;

    public WetClayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WET_CLAY_ENTITY, pos, state);
    }

    // metoda statyczna zgodna z BlockEntityTicker: (World, BlockPos, BlockState, BlockEntity)
    public static void tick(World world, BlockPos pos, BlockState state, WetClayBlockEntity be) {
        if (world.isClient) return;
        be.tickServer((ServerWorld) world, pos, state);
    }

    private void tickServer(ServerWorld world, BlockPos pos, BlockState state) {
        tickCounter++;
        if (tickCounter < STAGE_TICKS) return;
        tickCounter = 0;

        int current = state.get(WetClayBlock.DRYING_STAGE);
        if (current < WetClayBlock.MAX_STAGE) {
            int next = current + 1;
            BlockState nextState = state.with(WetClayBlock.DRYING_STAGE, next);
            world.setBlockState(pos, nextState, 3);
            this.markDirty();
        }
    }
    public int getTicksToNextStage() {
        return STAGE_TICKS - tickCounter;
    }
    public int getCurrentTicks() {
        return tickCounter;
    }
    public int getStageTicks() {
        return STAGE_TICKS;
    }
}