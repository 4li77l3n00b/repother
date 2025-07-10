package org.ctrwaz.repother.data;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.ctrwaz.repother.Repother;
import org.ctrwaz.repother.reg.ModBlocks;

import java.util.Map;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Repother.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (Map.Entry<ResourceLocation, Block> entry : ModBlocks.DYNAMICALLY_REGISTERED_DUST_LAYERS.entrySet()) {
            ResourceLocation blockId = entry.getKey();
            withExistingParent(blockId.getPath(), modLoc("item/dust"));
        }
    }
}
