package org.ctrwaz.repother.mixin;

import com.endertech.minecraft.mods.adpother.blocks.Pollutant;
import net.mehvahdjukaar.supplementaries.common.block.blocks.AshLayerBlock;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.ctrwaz.repother.Config.*;
@Mixin(Pollutant.class)
public abstract class DustPrecipitationMixin {
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("Repother");
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand, CallbackInfo ci) {
        Block dustBlock = state.getBlock();
        if (dustPrecipitation) {
            if (precipitablePollutants.contains(dustBlock.asItem())) {
                BlockPos neighborPos = pos.relative(Direction.DOWN);
                BlockState blockstate = level.getBlockState(neighborPos);
                Block blockBelow = blockstate.getBlock();
                if (!blockstate.isAir()) {
                    LOGGER.info("[repother-debug]dust pollutant satisfies precipitation condition, checking if it's the lucky one");
                    if (rand.nextInt(100) < chanceOfDissipation) {
                        level.removeBlock(pos, true);
                        if (blockBelow instanceof AshLayerBlock) {
                            int layers = blockstate.getValue(AshLayerBlock.LAYERS);
                            if (layers < 8) {
                                level.setBlockAndUpdate(neighborPos, ModRegistry.ASH_BLOCK.get().defaultBlockState().setValue(AshLayerBlock.LAYERS, layers + 1));
                            } else {
                                level.setBlockAndUpdate(pos, ModRegistry.ASH_BLOCK.get().defaultBlockState().setValue(AshLayerBlock.LAYERS, 1));
                            }
                        } else {
                            level.setBlockAndUpdate(pos, ModRegistry.ASH_BLOCK.get().defaultBlockState().setValue(AshLayerBlock.LAYERS, 1));
                        }
                        LOGGER.info("[repother-debug]pollutant precipitated");
                        return;
                    }
                }
            }
        }
    }
}
