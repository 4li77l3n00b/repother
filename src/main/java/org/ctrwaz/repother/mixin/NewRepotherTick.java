package org.ctrwaz.repother.mixin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.endertech.common.CommonCollect;
import com.endertech.common.IntBounds;
import com.endertech.common.CommonMath.Random;
import com.endertech.minecraft.forge.blocks.ForgeBlock;
import com.endertech.minecraft.forge.blocks.IPollutant;
import com.endertech.minecraft.forge.blocks.ISmokeContainer;
import com.endertech.minecraft.forge.configs.BlockStateList;
import com.endertech.minecraft.forge.configs.ColorARGB;
import com.endertech.minecraft.forge.configs.IForgeEnum;
import com.endertech.minecraft.forge.configs.MultiConfigProperty;
import com.endertech.minecraft.forge.configs.UnitConfig;
import com.endertech.minecraft.forge.configs.MultiConfigProperty.FloatProperty;
import com.endertech.minecraft.forge.configs.MultiConfigProperty.IntProperty;
import com.endertech.minecraft.forge.core.IPostInit;
import com.endertech.minecraft.forge.math.GameBounds;
import com.endertech.minecraft.forge.math.Percentage;
import com.endertech.minecraft.forge.world.BiomeId;
import com.endertech.minecraft.forge.world.ChunkBounds;
import com.endertech.minecraft.forge.world.DimensionId;
import com.endertech.minecraft.forge.world.Dimensions;
import com.endertech.minecraft.forge.world.GameWorld;
import com.endertech.minecraft.forge.world.GameWorld.Directions;
import com.endertech.minecraft.forge.world.GameWorld.Positions;
import com.endertech.minecraft.mods.adpother.AdPother;
import com.endertech.minecraft.mods.adpother.config.FilterMaterialList;
import com.endertech.minecraft.mods.adpother.entities.AbstractCarrier;
import com.endertech.minecraft.mods.adpother.entities.GasEntity;
import com.endertech.minecraft.mods.adpother.entities.AbstractCarrier.SpawnTime;
import com.endertech.minecraft.mods.adpother.impacts.AbstractPollutionImpacts;
import com.endertech.minecraft.mods.adpother.impacts.EnvironmentalImpacts;
import com.endertech.minecraft.mods.adpother.impacts.AbstractPollutionImpacts.ImpactType;
import com.endertech.minecraft.mods.adpother.pollution.IFilterFrame;
import com.endertech.minecraft.mods.adpother.pollution.IStorageItem;
import com.endertech.minecraft.mods.adpother.pollution.PollutionInfo;
import com.endertech.minecraft.mods.adpother.pollution.Spread;
import com.endertech.minecraft.mods.adpother.pollution.WorldData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Optional;


import com.endertech.minecraft.mods.adpother.blocks.Pollutant;

import com.endertech.minecraft.mods.adpother.blocks.Pollutant.Density;

import static com.endertech.minecraft.mods.adpother.blocks.Pollutant.DENSITY;
import static org.ctrwaz.repother.Config.chanceOfElimination;

@Mixin(Pollutant.class)
public abstract class NewRepotherTick{
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("Repother");
    @Inject(method = "tick", at = @At("HEAD")) // 在 tick 方法的开头注入
    private void onTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand, CallbackInfo ci) {
        // 你的自定义逻辑
        Block block = state.getBlock();
        LOGGER.info("[omgomg]random tick hit, inspecting pollutant block at:{} {} {} |{}",pos.getX(),pos.getY(),pos.getZ(),block.getName());
        if (rand.nextInt(100) < chanceOfElimination && state.getValue(DENSITY) == Density.LIGHT) {
            LOGGER.info("[omgomg]satisfies elimination condition");// 0-9 为 10% 概率
            Pollutant pollutant = (Pollutant) (Object) this;
            level.removeBlock(pos,true);
           // level.removeBlock(pos,false);
            //pollutant.destroy(level, pos, state); // 调用 destroy 方法移除方块
            LOGGER.info("[omgomg]pollutant eliminated");
            return;
        }

    }
}
