package org.ctrwaz.repother.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.ctrwaz.repother.block.DustLayerBlock;
import org.ctrwaz.repother.reg.ModEntities;

public class DustLayerFallingBlockEntity extends FallingBlockEntity {

    private static final EntityDataAccessor<BlockState> DATA_BLOCK_STATE =
            SynchedEntityData.defineId(DustLayerFallingBlockEntity.class, EntityDataSerializers.BLOCK_STATE);

    private BlockState blockStateOnServer = Blocks.AIR.defaultBlockState();


    public DustLayerFallingBlockEntity(EntityType<? extends FallingBlockEntity> type, Level level) {
        super(type, level);
    }

    public DustLayerFallingBlockEntity(Level level, double x, double y, double z, BlockState blockState) {
        this(ModEntities.DUST_LAYER_FALLING_ENTITY.get(), level);
        this.blockStateOnServer = blockState; // 在服务器端设置
        this.entityData.set(DATA_BLOCK_STATE, blockState); // 同时设置同步数据
        this.blocksBuilding = true;
        this.setPos(x, y, z);
        this.setStartPos(this.blockPosition());
    }

    public static DustLayerFallingBlockEntity fall(Level level, BlockPos pos, BlockState state) {
        DustLayerFallingBlockEntity entity = new DustLayerFallingBlockEntity(level,
                pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, state);
        level.setBlock(pos, state.getFluidState().createLegacyBlock(), 3);
        level.addFreshEntity(entity);
        return entity;
    }

    @Override
    public BlockState getBlockState() {
        return this.entityData.get(DATA_BLOCK_STATE);
    }

    @Override
    public void tick() {
        if (this.getBlockState().isAir()) {
            this.discard();
            return;
        }

        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();

        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());


        if (!this.level().isClientSide) {
            BlockPos pos = this.blockPosition();

            if (this.onGround()) {
                BlockState stateOnGround = this.level().getBlockState(pos);

                if (stateOnGround.is(this.getBlockState().getBlock())) {
                    int groundLayers = stateOnGround.getValue(DustLayerBlock.LAYERS);
                    if (groundLayers < DustLayerBlock.MAX_LAYERS) {
                        int fallingLayers = this.getBlockState().getValue(DustLayerBlock.LAYERS);
                        int combined = groundLayers + fallingLayers;
                        int target = Mth.clamp(combined, 1, DustLayerBlock.MAX_LAYERS);
                        int remaining = combined - target;

                        this.level().setBlock(pos, stateOnGround.setValue(DustLayerBlock.LAYERS, target), 3);

                        if (remaining > 0) {
                            BlockState remainingState = this.getBlockState().setValue(DustLayerBlock.LAYERS, remaining);
                            this.level().setBlock(pos.above(), remainingState, 3);                        }

                        this.discard();
                        return;
                    }
                }

                if (this.getBlockState().canSurvive(this.level(), pos) && this.level().isEmptyBlock(pos)) {
                    this.level().setBlock(pos, this.getBlockState(), 3);
                    this.discard();
                    return;
                }

                if (this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                    Block.popResource(this.level(), pos, new ItemStack(this.getBlockState().getBlock()));
                }
                this.discard();

            } else if (this.time > 100 && (pos.getY() <= this.level().getMinBuildHeight() || pos.getY() > this.level().getMaxBuildHeight()) || this.time > 600) {
                if (this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                    Block.popResource(this.level(), this.blockPosition(), new ItemStack(this.getBlockState().getBlock()));
                }
                this.discard();
            }
        }

        this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_BLOCK_STATE, Blocks.AIR.defaultBlockState());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        BlockState loadedState = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), compound.getCompound("BlockState"));
        this.entityData.set(DATA_BLOCK_STATE, loadedState);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.put("BlockState", NbtUtils.writeBlockState(this.getBlockState()));
    }
}
