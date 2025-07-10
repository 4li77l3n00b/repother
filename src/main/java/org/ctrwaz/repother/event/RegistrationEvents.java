package org.ctrwaz.repother.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import org.ctrwaz.repother.Repother;
import org.ctrwaz.repother.block.DustLayerBlock;
import org.ctrwaz.repother.reg.ModBlocks;

import java.lang.reflect.Method;
import java.util.Map;



@Mod.EventBusSubscriber(modid = Repother.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistrationEvents {

    private static Class<?> pollutantClassCache = null;
    private static Method getColorMethodCache = null;
    private static Method getArgbMethodCache = null;
    private static boolean attemptedToLoadData = false;

    @SubscribeEvent
    public static void onRegisterEvent(final RegisterEvent event) {

        if (event.getRegistryKey().equals(ForgeRegistries.Keys.BLOCKS)) {
            registerDynamicDustBlocks(event.getForgeRegistry());
        }

        if (event.getRegistryKey().equals(ForgeRegistries.Keys.ITEMS)) {
            registerDynamicDustBlockItems(event.getForgeRegistry());
        }
    }

    private static void registerDynamicDustBlocks(IForgeRegistry<Block> registry) {
        loadPollutantData();

        if (pollutantClassCache == null || getColorMethodCache == null || getArgbMethodCache == null) {
            return;
        }

        for (Block existingBlock : ForgeRegistries.BLOCKS) {
            if (pollutantClassCache.isAssignableFrom(existingBlock.getClass())) {
                ResourceLocation sourceId = ForgeRegistries.BLOCKS.getKey(existingBlock);
                if (sourceId == null || sourceId.getNamespace().equals("minecraft")) continue;

                int color;
                try {
                    Object colorArgbObject = getColorMethodCache.invoke(existingBlock);

                    if (colorArgbObject == null) {
                        throw new NullPointerException("getColor() method returned null for block " + sourceId);
                    }

                    Object finalResult = getArgbMethodCache.invoke(colorArgbObject);
                    color = (int) finalResult;

                } catch (Exception e) {
                    System.err.println("Failed to get ARGB color via reflection for " + sourceId + ". Using default MapColor. Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                    color = existingBlock.defaultBlockState().getMapColor(null, null).col; // 回退到MapColor
                }

                ResourceLocation newBlockId = new ResourceLocation(Repother.MODID, sourceId.getPath() + "_pollutant");

                DustLayerBlock newDustBlock = new DustLayerBlock(
                        BlockBehaviour.Properties.of().mapColor(findClosestMapColor(color)).strength(0.5F).sound(SoundType.SAND).randomTicks(),
                        color
                );

                registry.register(newBlockId, newDustBlock);
                ModBlocks.DYNAMICALLY_REGISTERED_DUST_LAYERS.put(newBlockId, newDustBlock);
                ModBlocks.POLLUTANT_TO_DUST_MAP.put(existingBlock, newDustBlock);
            }
        }
    }

    private static void registerDynamicDustBlockItems(IForgeRegistry<Item> registry) {
        for (Map.Entry<ResourceLocation, Block> entry : ModBlocks.DYNAMICALLY_REGISTERED_DUST_LAYERS.entrySet()) {
            ResourceLocation blockId = entry.getKey();
            Block blockInstance = entry.getValue();

            BlockItem blockItem = new BlockItem(blockInstance, new Item.Properties());

            registry.register(blockId, blockItem);
        }
    }

    private static void loadPollutantData() {
        if (!attemptedToLoadData) {
            attemptedToLoadData = true;
            if (ModList.get().isLoaded("adpother")) {
                try {
                    pollutantClassCache = Class.forName("com.endertech.minecraft.mods.adpother.blocks.Pollutant");

                    getColorMethodCache = pollutantClassCache.getMethod("getColor");

                    Class<?> colorArgbClass = getColorMethodCache.getReturnType();

                    getArgbMethodCache = colorArgbClass.getMethod("getARGB");

                    if (getArgbMethodCache.getReturnType() != int.class) {
                        System.err.println("Found 'getARGB' method, but its return type is not 'int'!");
                        getArgbMethodCache = null;
                    }

                } catch (ClassNotFoundException e) {
                    System.err.println("Mod 'adpother' is loaded, but a required class was not found.");
                } catch (NoSuchMethodException e) {
                    System.err.println("A required method (getColor or getARGB) was not found in 'adpother' classes. Error: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("An unexpected error occurred while loading data from 'adpother'. " + e.getMessage());
                }
            }
        }
    }
    private static MapColor findClosestMapColor(int rgbColor) {
        int targetRed = (rgbColor >> 16) & 0xFF;
        int targetGreen = (rgbColor >> 8) & 0xFF;
        int targetBlue = rgbColor & 0xFF;

        MapColor closestColor = MapColor.NONE;
        double minDistanceSq = Double.MAX_VALUE;

        for (int i = 0; i < 64; i++) {
            MapColor currentColor = MapColor.byId(i);

            if (currentColor == null || currentColor == MapColor.NONE) {
                continue;
            }

            int currentRed = (currentColor.col >> 16) & 0xFF;
            int currentGreen = (currentColor.col >> 8) & 0xFF;
            int currentBlue = currentColor.col & 0xFF;

            double distanceSq = Math.pow(targetRed - currentRed, 2) +
                    Math.pow(targetGreen - currentGreen, 2) +
                    Math.pow(targetBlue - currentBlue, 2);

            if (distanceSq < minDistanceSq) {
                minDistanceSq = distanceSq;
                closestColor = currentColor;
            }
        }

        return closestColor;
    }
}
