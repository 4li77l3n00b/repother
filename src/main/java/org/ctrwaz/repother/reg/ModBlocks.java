package org.ctrwaz.repother.reg;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.ctrwaz.repother.Repother;

import java.util.HashMap;
import java.util.Map;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Repother.MODID);

    public static final Map<ResourceLocation, Block> DYNAMICALLY_REGISTERED_DUST_LAYERS = new HashMap<>();
    public static final Map<Block, Block> POLLUTANT_TO_DUST_MAP = new HashMap<>();
}