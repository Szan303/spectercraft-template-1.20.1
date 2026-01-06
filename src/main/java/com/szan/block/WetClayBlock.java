package com.szan.block;

import com.szan.block.entity.WetClayBlockEntity;
import com.szan.registry.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft. block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block. entity.BlockEntityType;
import net.minecraft.state.StateManager;
import net. minecraft.state.property.IntProperty;
import net.minecraft. util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft. world.World;
import org.jetbrains.annotations. Nullable;

public class WetClayBlock extends BlockWithEntity {
    public static final IntProperty DRYING_STAGE = IntProperty.of("drying_stage", 0, 4);

    // Custom hitbox - dopasowany do modelu (6×3×12)
    // Coordinates: [minX, minY, minZ, maxX, maxY, maxZ] w pixelach (0-16)
    private static final VoxelShape SHAPE = Block.createCuboidShape(
            5.0,  // minX (5 pixeli od lewej)
            0.0,  // minY (na ziemi)
            2.0,  // minZ (2 pixele od przodu)
            11.0, // maxX (11 pixeli od lewej = szerokość 6)
            3.0,  // maxY (wysokość 3 pixele)
            14.0  // maxZ (14 pixeli od przodu = głębokość 12)
    );

    public WetClayBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(DRYING_STAGE, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(DRYING_STAGE);
    }

    // ========== CUSTOM HITBOX ==========
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    // ========== BLOCK ENTITY ==========
    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new WetClayBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : checkType(type, ModBlockEntities.WET_CLAY_ENTITY, WetClayBlockEntity::tick);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
}