package org.ctrwaz.repother.data;


import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.ctrwaz.repother.Repother;
import org.ctrwaz.repother.block.DustLayerBlock;
import org.ctrwaz.repother.reg.ModBlocks;

public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Repother.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        for (Block block : ModBlocks.DYNAMICALLY_REGISTERED_DUST_LAYERS.values()) {
            registerDustLayerState(block);
        }
    }

    private void registerDustLayerState(Block block) {
        getVariantBuilder(block).forAllStates(state -> {

            int layers = state.getValue(DustLayerBlock.LAYERS);

            int height = layers * 2;


            var modelFile = models().getExistingFile(modLoc("block/dust_" + height));


            return ConfiguredModel.builder().modelFile(modelFile).build();
        });
    }
}
