package br.com.brforgers.mods.ducts;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class DuctBlock extends BlockWithEntity {
    public DuctBlock(){
        this(FabricBlockSettings.of(Material.METAL, MapColor.IRON_GRAY).breakByHand(true).breakByTool(FabricToolTags.PICKAXES).strength(1.0F, 6.0F).sounds(BlockSoundGroup.METAL).nonOpaque());
        this.setDefaultState(this.getDefaultState().with(OUTPUT, Direction.NORTH));
    }
    protected DuctBlock(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DuctBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null :checkType(type, DuctBlockEntity.TYPE, DuctBlockEntity::tick);

    }

    private HashMap<BlockState, VoxelShape> shapeCache = new HashMap<>();

    @Override
    public void appendProperties(StateManager.Builder<Block, BlockState> propContainerBuilder) {
        propContainerBuilder.add(
                OUTPUT
                );
    }
    @Override
    public VoxelShape getOutlineShape(
            BlockState state,
            BlockView view,
            BlockPos blockPos,
            ShapeContext verticalEntityPosition
    ){
        return shapeCache.computeIfAbsent(state, x-> {
            VoxelShape core = Shapes.coreCube;
            VoxelShape output = Shapes.outputCubes.get(x.get(OUTPUT));
            return VoxelShapes.union(core, output);
        });
    }
    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(
                OUTPUT,
                rotation.rotate(state.get(OUTPUT))
        );
    }
    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.with(
                OUTPUT,
                mirror.apply(state.get(OUTPUT))
        );
    }
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return getDefaultState().with(OUTPUT, context.getSide().getOpposite());
    }

    private Boolean canConnect(BlockState other, Direction dirToOther)  {
        return (other.isOf(Blocks.HOPPER)|| other.isOf(Ducts.DUCT_BLOCK)) &&(
                (other.contains(Properties.FACING) && other.get(Properties.FACING) == dirToOther.getOpposite())||
                        (other.contains(Properties.HORIZONTAL_FACING) && other.get(Properties.HORIZONTAL_FACING) == dirToOther.getOpposite())
                        ||(other.contains(Properties.HOPPER_FACING) && other.get(Properties.HOPPER_FACING) == dirToOther.getOpposite())
                );
    }

    @Override
    public ActionResult onUse(
             BlockState state,
             World world,
             BlockPos pos,
             PlayerEntity player,
             Hand hand,
             BlockHitResult blockHitPos
    )  {
        if (player.getStackInHand(hand).getItem() == this.asItem() && player.isSneaky())
            return ActionResult.PASS;

        return ActionResult.SUCCESS;
    }

    @Override
    public void onStateReplaced(
            BlockState state1, World world, BlockPos pos, BlockState state2, boolean moved
    ) {
        if (state1.getBlock() != state2.getBlock()) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof DuctBlockEntity) {
                ItemScatterer.spawn(world, pos, ((DuctBlockEntity)entity).inventory);
                world.updateComparators(pos, this);
            }
        }
        super.onStateReplaced(state1, world, pos, state2, moved);
    }

    @Override
    public boolean hasComparatorOutput( BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput( BlockState state,World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    @Override public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

        public static final DirectionProperty OUTPUT = Properties.FACING;

    private class Shapes {
        static final VoxelShape coreCube = createCuboidShape(4.0, 4.0, 4.0, 12.0, 12.0, 12.0);
                static final Map<Direction, VoxelShape> outputCubes = Map.of(
                Direction.NORTH , createCuboidShape(6.0, 6.0, 0.0, 10.0, 10.0, 4.0), //Z
                Direction.EAST , createCuboidShape(12.0, 6.0, 6.0, 16.0, 10.0, 10.0), //X
                Direction.SOUTH , createCuboidShape(6.0, 6.0, 12.0, 10.0, 10.0, 16.0), //Z
                Direction.WEST , createCuboidShape(0.0, 6.0, 6.0, 4.0, 10.0, 10.0), //X
                Direction.DOWN , createCuboidShape(6.0, 0.0, 6.0, 10.0, 4.0, 10.0), //Y
                Direction.UP , createCuboidShape(6.0, 12.0, 6.0, 10.0, 16.0, 10.0) //Y NEW
        );
    }
}