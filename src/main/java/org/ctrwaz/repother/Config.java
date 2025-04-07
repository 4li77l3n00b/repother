package org.ctrwaz.repother;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = Repother.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue NATURAL_DECAY = BUILDER.comment("Should Pollutants naturally dissipate?").define("naturalDecay", true);

    private static final ForgeConfigSpec.IntValue CHANCE_OF_ELIMINATION = BUILDER.comment("Chance of Elimination of Pollutant blocks").defineInRange("chanceOfElimination", 10, 0, 100);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static Boolean naturalDecay;

    public static int chanceOfElimination;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        naturalDecay = NATURAL_DECAY.get();
        chanceOfElimination = CHANCE_OF_ELIMINATION.get();
    }
}
