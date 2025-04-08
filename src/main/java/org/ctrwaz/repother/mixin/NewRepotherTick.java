package org.ctrwaz.repother.mixin;

import com.endertech.minecraft.forge.world.BiomeId;
import net.mehvahdjukaar.supplementaries.common.block.blocks.AshLayerBlock;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.Direction;
import org.ctrwaz.repother.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import com.endertech.minecraft.mods.adpother.blocks.Pollutant;

import com.endertech.minecraft.mods.adpother.blocks.Pollutant.Density;

import static com.endertech.minecraft.mods.adpother.blocks.Pollutant.DENSITY;
import static org.ctrwaz.repother.Config.*;

@Mixin(Pollutant.class)
public abstract class NewRepotherTick{
    @Shadow public abstract int getConcentrationAltitudeIn(BiomeId biome);

    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("Repother");
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand, CallbackInfo ci) {
        Block block = state.getBlock();
        LOGGER.info("[repother-debug]random tick hit, inspecting pollutant block at:{} {} {} |{}", pos.getX(), pos.getY(), pos.getZ(), block.getName());
        if (gasDissipation) {
            if (dissipablePollutants.contains(block.asItem())) {
                if (state.getValue(DENSITY) == Density.LIGHT && pos.getY() == this.getConcentrationAltitudeIn(BiomeId.from(level, pos))) {
                    LOGGER.info("[repother-debug]gas pollutant satisfies elimination condition, checking if it's the lucky one");
                    if (rand.nextInt(100) < chanceOfDissipation) {
                        level.removeBlock(pos, true);
                        LOGGER.info("[repother-debug]pollutant eliminated");
                        return;
                    }
                }
            }
        }

        if (Util.enablePrecipitation && dustPrecipitation) {
            if (precipitablePollutants.contains(block.asItem())) {
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
