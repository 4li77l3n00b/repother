package org.ctrwaz.repother.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.ctrwaz.repother.entity.DustLayerFallingBlockEntity;

import javax.annotation.Nullable;

import static org.ctrwaz.repother.Config.chanceOfRainWashing;

public class DustLayerBlock extends FallingBlock {

    public static final int MAX_LAYERS = 8;
    public static final IntegerProperty LAYERS = BlockStateProperties.LAYERS;
    protected static final VoxelShape[] SHAPE_BY_LAYER = new VoxelShape[MAX_LAYERS + 1];

    private final int dustColor;

    static {
        SHAPE_BY_LAYER[0] = Shapes.empty();
        for (int i = 1; i <= MAX_LAYERS; i++) {
            SHAPE_BY_LAYER[i] = Block.box(0.0D, 0.0D, 0.0D, 16.0D, i * 2.0D, 16.0D);
        }
    }

    public DustLayerBlock(Properties properties, int dustColor) {
        super(properties);
        this.dustColor = dustColor;
        this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, 1));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.getItem() instanceof ShovelItem) {
            if (!level.isClientSide) {
                int currentLayers = state.getValue(LAYERS);
                if (currentLayers > 1) {
                    level.setBlock(pos, state.setValue(LAYERS, currentLayers - 1), 3);
                } else {
                    level.removeBlock(pos, false);
                }
                ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(this.asItem()));
                itemEntity.setDefaultPickUpDelay();
                level.addFreshEntity(itemEntity);
                heldItem.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                level.playSound(null, pos, this.soundType.getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    private boolean tryToMergeDownward(BlockState state, LevelAccessor level, BlockPos pos) {
        BlockPos posBelow = pos.below();
        BlockState stateBelow = level.getBlockState(posBelow);

        if (stateBelow.is(this)) {
            int bottomLayers = stateBelow.getValue(LAYERS);
            if (bottomLayers < MAX_LAYERS) {
                int topLayers = state.getValue(LAYERS);

                int spaceBelow = MAX_LAYERS - bottomLayers;
                int layersToMove = Math.min(topLayers, spaceBelow);

                if (layersToMove > 0) {
                    level.setBlock(posBelow, stateBelow.setValue(LAYERS, bottomLayers + layersToMove), 3);

                    int remainingTopLayers = topLayers - layersToMove;
                    if (remainingTopLayers <= 0) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    } else {
                        level.setBlock(pos, state.setValue(LAYERS, remainingTopLayers), 3);
                    }

                    level.playSound(null, pos, this.soundType.getStepSound(), SoundSource.BLOCKS, 0.7F, 0.9F + level.getRandom().nextFloat() * 0.2F);

                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onBlockStateChange(LevelReader level, BlockPos pos, BlockState oldState, BlockState newState) {
        super.onBlockStateChange(level, pos, oldState, newState);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (level.isClientSide && (entity.xOld != entity.getX() || entity.zOld != entity.getZ())) {
            if (level.random.nextInt(3) == 0) {
                level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, state),
                        entity.getX(), pos.getY() + 0.5D, entity.getZ(),
                        level.random.nextGaussian() * 0.02D,
                        level.random.nextGaussian() * 0.02D,
                        level.random.nextGaussian() * 0.02D);
            }
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter world, BlockPos pos, PathComputationType type) {
        return type == PathComputationType.LAND && state.getValue(LAYERS) < 5;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_LAYER[state.getValue(LAYERS)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_LAYER[Math.max(0, state.getValue(LAYERS))];
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!tryToMergeDownward(state, world, pos)) {
            world.scheduleTick(pos, this, this.getDelayAfterPlace());
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (!state.canSurvive(level, currentPos)) {
            level.scheduleTick(currentPos, this, this.getDelayAfterPlace());
        }
        this.tryToMergeDownward(state, level, currentPos);
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.getFluidState(pos.below()).isEmpty()) {
            level.destroyBlock(pos, true);
            return;
        }

        if (level.isEmptyBlock(pos.below()) && pos.getY() >= level.getMinBuildHeight()) {
            DustLayerFallingBlockEntity.fall(level, pos, state);
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.isRainingAt(pos.above()) && level.getBiome(pos).value().getPrecipitationAt(pos) == Biome.Precipitation.RAIN) {
            if (random.nextInt(100) < chanceOfRainWashing) {
                this.removeOneLayer(state, pos, level);
                level.sendParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5, 3, 0.2, 0.1, 0.2, 0.1);
            }
        }
    }

    private void removeOneLayer(BlockState state, BlockPos pos, Level level) {
        int levels = (Integer)state.getValue(LAYERS);
        if (levels > 1) {
            level.setBlockAndUpdate(pos, (BlockState)state.setValue(LAYERS, levels - 1));
        } else {
            level.removeBlock(pos, false);
        }

    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        if (useContext.getItemInHand().getItem() instanceof BlockItem blockItem) {
            if (blockItem.getBlock() == this) {
                return state.getValue(LAYERS) < MAX_LAYERS;
            }
        }
        return false;
    }

    @Override
    public boolean canBeReplaced(BlockState state, Fluid fluid) {
        return state.getValue(LAYERS) < 3;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (state.is(this)) {
            int i = state.getValue(LAYERS);
            return state.setValue(LAYERS, Math.min(MAX_LAYERS, i + 1));
        }
        return super.getStateForPlacement(context);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LAYERS);
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter reader, BlockPos pos) {
        return this.dustColor;
    }

    public int getColor() {
        return this.dustColor;
    }
}
