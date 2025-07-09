package org.ctrwaz.repother.mixin;

import com.endertech.minecraft.mods.adpother.blocks.PollutedWater;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Supplier;

import static org.ctrwaz.repother.Config.chanceOfRefreshing;
import static org.ctrwaz.repother.Config.waterRefreshing;

@Mixin(PollutedWater.class)
public class WaterRefreshingMixin extends LiquidBlock {
    public WaterRefreshingMixin(Supplier<? extends FlowingFluid> pFluid, Properties pProperties) {
        super(pFluid, pProperties);
    }

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;of()Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;"
            )
    )
    private static BlockBehaviour.Properties enableRandomTicks() {
        return BlockBehaviour.Properties.of().randomTicks();
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (waterRefreshing) {
            if (random.nextInt(100) < chanceOfRefreshing) {
                if (state.getValue(LiquidBlock.LEVEL) == 0) {
                    level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
                }
            }
        }
    }
}
