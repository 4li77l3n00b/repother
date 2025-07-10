package org.ctrwaz.repother.mixin;

import com.endertech.minecraft.forge.world.BiomeId;
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
public abstract class GasDissipationMixin {
    @Shadow public abstract int getConcentrationAltitudeIn(BiomeId biome);

    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("Repother");
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand, CallbackInfo ci) {
        Block gasBlock = state.getBlock();
        if (gasDissipation) {
            if (dissipablePollutants.contains(gasBlock.asItem())) {
                if (state.getValue(DENSITY) == Density.LIGHT && pos.getY() == this.getConcentrationAltitudeIn(BiomeId.from(level, pos))) {
                    if (rand.nextInt(100) < chanceOfDissipation) {
                        level.removeBlock(pos, true);
                        ci.cancel();
                    }
                }
            }
        }
    }
}
