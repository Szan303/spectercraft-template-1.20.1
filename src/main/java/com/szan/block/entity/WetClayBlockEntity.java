package com.szan.block.entity;

import com.szan.block.WetClayBlock;
import com.szan.registry. ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block. entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net. minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WetClayBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpecterCraft/WetClay");

    private int dryingTime = 0;
    private static final int TIME_PER_STAGE = 200; // 10 sekund (test mode)

    public WetClayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WET_CLAY_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, WetClayBlockEntity blockEntity) {
        if (world.isClient) return;

        boolean hasDirectSunlight = world.isSkyVisible(pos. up()) && world.isDay() && !world.isRaining();

        if (hasDirectSunlight) {
            blockEntity. dryingTime++;

            int currentStage = state.get(WetClayBlock.DRYING_STAGE);
            int targetStage = Math.min(blockEntity.dryingTime / TIME_PER_STAGE, 4); // ← MAX 4!

            if (targetStage > currentStage) {
                world.setBlockState(pos, state.with(WetClayBlock.DRYING_STAGE, targetStage));
                LOGGER.info("[WetClay] Stage {} -> {}", currentStage, targetStage);
                blockEntity.markDirty();

                // ========== ZATRZYMAJ NA STAGE 4 ==========
                if (targetStage == 4) {
                    LOGGER.info("[WetClay] Osiągnięto maksymalny stage (4) - sucha glina!");
                    // NIE zamieniaj w brick!
                }
            }
        } else if (world.isRaining() && world.isSkyVisible(pos.up())) {
            // Deszcz spowalnia wysychanie
            if (blockEntity.dryingTime > 0) {
                blockEntity.dryingTime = Math.max(0, blockEntity. dryingTime - 2);

                int currentStage = state.get(WetClayBlock.DRYING_STAGE);
                int targetStage = blockEntity.dryingTime / TIME_PER_STAGE;

                if (targetStage < currentStage) {
                    world.setBlockState(pos, state.with(WetClayBlock.DRYING_STAGE, targetStage));
                    LOGGER.info("[WetClay] Deszcz!  Stage {} -> {}", currentStage, targetStage);
                    blockEntity.markDirty();
                }
            }
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.dryingTime = nbt.getInt("DryingTime");
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("DryingTime", this.dryingTime);
    }
}