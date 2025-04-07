package org.ctrwaz.repother.mixin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import com.endertech.minecraft.mods.adpother.blocks.Pollutant;

import com.endertech.minecraft.mods.adpother.blocks.Pollutant.Density;

import static com.endertech.minecraft.mods.adpother.blocks.Pollutant.DENSITY;
import static org.ctrwaz.repother.Config.naturalDecay;
import static org.ctrwaz.repother.Config.chanceOfElimination;

@Mixin(Pollutant.class)
public abstract class NewRepotherTick{
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("Repother");
    @Inject(method = "tick", at = @At("HEAD")) // 在 tick 方法的开头注入
    private void onTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand, CallbackInfo ci) {
        // 你的自定义逻辑
        if (naturalDecay) {
            Block block = state.getBlock();
            LOGGER.info("[omgomg]random tick hit, inspecting pollutant block at:{} {} {} |{}", pos.getX(), pos.getY(), pos.getZ(), block.getName());
            if (rand.nextInt(100) < chanceOfElimination && state.getValue(DENSITY) == Density.LIGHT) {
                LOGGER.info("[omgomg]satisfies elimination condition");// 0-9 为 10% 概率
                level.removeBlock(pos, true);
                // level.removeBlock(pos,false);
                //pollutant.destroy(level, pos, state); // 调用 destroy 方法移除方块
                LOGGER.info("[omgomg]pollutant eliminated");
                return;
            }
        }
    }
}
