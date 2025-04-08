package org.ctrwaz.repother;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = Repother.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue GAS_DISSIPATION = BUILDER.comment("Enable Gas Dissipation").define("gasDissipation", true);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> DISSIPABLE_POLLUTANTS = BUILDER.comment(" - List of pollutants that can disperse like gases").defineListAllowEmpty("dissipablePollutants", List.of("adpother:carbon", "adpother:sulfur"), Config::validateItemName);

    private static final ForgeConfigSpec.IntValue CHANCE_OF_ELIMINATION = BUILDER.comment(" - Chance of Eliminating Pollutant blocks").defineInRange("chanceOfDissipation", 10, 0, 100);

    private static final ForgeConfigSpec.BooleanValue DUST_PRECIPITATION = BUILDER.comment("Enable Dust Precipitation").define("dustPrecipitation", true);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> PRECIPITABLE_POLLUTANTS = BUILDER.comment(" - List of pollutants that can precipitate like dust").defineListAllowEmpty("precipitablePollutants", List.of("adpother:dust"), Config::validateItemName);

    private static final ForgeConfigSpec.IntValue CHANCE_OF_PRECIPITATION = BUILDER.comment(" - Chance of generating dust").defineInRange("chanceOfPrecipitation", 10, 0, 100);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static Boolean gasDissipation;

    public static Set<Item> dissipablePollutants;

    public static int chanceOfDissipation;

    public static Boolean dustPrecipitation;

    public static Set<Item> precipitablePollutants;

    public static int chanceOfPrecipitation;

    private static boolean validateItemName(final Object obj) {
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        gasDissipation = GAS_DISSIPATION.get();
        dissipablePollutants = DISSIPABLE_POLLUTANTS.get().stream().map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName))).collect(Collectors.toSet());
        chanceOfDissipation = CHANCE_OF_ELIMINATION.get();
        dustPrecipitation = DUST_PRECIPITATION.get();
        precipitablePollutants = PRECIPITABLE_POLLUTANTS.get().stream().map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName))).collect(Collectors.toSet());
        chanceOfPrecipitation = CHANCE_OF_PRECIPITATION.get();
    }
}