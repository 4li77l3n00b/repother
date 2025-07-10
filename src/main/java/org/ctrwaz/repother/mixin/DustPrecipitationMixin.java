package org.ctrwaz.repother.mixin;

import com.endertech.minecraft.mods.adpother.blocks.Pollutant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.ctrwaz.repother.block.DustLayerBlock;
import org.ctrwaz.repother.reg.ModBlocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.google.common.primitives.Ints.min;
import static org.ctrwaz.repother.Config.*;
@Mixin(Pollutant.class)
public abstract class DustPrecipitationMixin {
    @Shadow @Final public static EnumProperty<Pollutant.Density> DENSITY;
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("Repother");
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand, CallbackInfo ci) {
        Block dustBlock = state.getBlock();
        if (dustPrecipitation) {
            Block pollutantBlock = state.getBlock();
            if (precipitablePollutants.contains(dustBlock.asItem())) {
                BlockPos neighborPos = pos.relative(Direction.DOWN);
                BlockState belowState = level.getBlockState(neighborPos);
                Block blockBelow = belowState.getBlock();
                if (!belowState.isAir() && !belowState.is(state.getBlock())) {
                    if (rand.nextInt(100) < min(chanceOfDissipation+10*(state.getValue(DENSITY).ordinal()), 100)) {
                        Block dustBlockToPlace = ModBlocks.POLLUTANT_TO_DUST_MAP.get(pollutantBlock);
                        if (dustBlockToPlace == null) {
                            return;
                        }

                        level.removeBlock(pos, false);

                        if (blockBelow instanceof DustLayerBlock) {
                            int layers = belowState.getValue(DustLayerBlock.LAYERS);
                            if (layers < DustLayerBlock.MAX_LAYERS) {
                                level.setBlockAndUpdate(neighborPos, belowState.setValue(DustLayerBlock.LAYERS, layers + 1));
                            } else {
                                level.setBlockAndUpdate(pos, dustBlockToPlace.defaultBlockState().setValue(DustLayerBlock.LAYERS, 1));
                            }
                        } else {
                            level.setBlockAndUpdate(pos, dustBlockToPlace.defaultBlockState().setValue(DustLayerBlock.LAYERS, 1));
                        }

                        ci.cancel();
                    }
                }
            }
        }
    }
}
