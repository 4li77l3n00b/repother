package org.ctrwaz.repother.reg;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class ModTags {

    public static final class Biomes {
        public static final TagKey<Biome> IS_REFRESHING =
                create("refreshing_biomes");

        private static TagKey<Biome> create(String name) {
            return TagKey.create(Registries.BIOME, new ResourceLocation("repother", name));
        }
    }
}
