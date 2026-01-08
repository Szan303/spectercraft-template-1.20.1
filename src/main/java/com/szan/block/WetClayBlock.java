package com.szan.block;

import com.szan.block.entity.WetClayBlockEntity;
import com.szan.registry.Block.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WetClayBlock extends BlockWithEntity {
    public static final IntProperty DRYING_STAGE = IntProperty.of("drying_stage", 0, 4);
    public static final int MAX_STAGE = 4;

    private static final VoxelShape SHAPE = Block.createCuboidShape(5.0, 0.0, 2.0, 11.0, 3.0, 14.0);

    public WetClayBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState().with(DRYING_STAGE, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(DRYING_STAGE);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

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

    /**
     * Zamiast nadpisywać getDroppedStacks (różne mappingi),
     * używamy afterBreak, które ma stabilną sygnaturę:
     * - Jeśli blok jest całkowicie suchy (stage >= MAX_STAGE), upuść cegłę.
     * - W przeciwnym razie wywołaj super, by normalnie upuścić blok.
     */
    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack) {
        if (!world.isClient) {
            int stage = state.get(DRYING_STAGE);
            if (stage >= MAX_STAGE) {
                // drop brick instead of block
                dropStack(world, pos, new ItemStack(Items.BRICK));
                // remove block (super.afterBreak would also handle xp etc.; nie chcemy double-drop)
                world.removeBlock(pos, false);
                return;
            }
        }
        super.afterBreak(world, player, pos, state, blockEntity, stack);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        // BlockEntity zajmuje się tickowaniem i progresją drying_stage
    }
}