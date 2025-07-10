package org.ctrwaz.repother.event;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.ctrwaz.repother.Repother;
import org.ctrwaz.repother.block.DustLayerBlock;
import org.ctrwaz.repother.reg.ModBlocks;
import org.ctrwaz.repother.reg.ModEntities;


@Mod.EventBusSubscriber(modid = Repother.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void registerEntityRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        // 这行代码是关键。它告诉Forge：
        // 当需要渲染我们的自定义实体 (DUST_LAYER_FALLING_ENTITY) 时，
        // 请使用原版的 FallingBlockRenderer 来创建渲染器实例。
        event.registerEntityRenderer(ModEntities.DUST_LAYER_FALLING_ENTITY.get(), FallingBlockRenderer::new);
    }

    @SubscribeEvent
    public static void registerBlockColors(final RegisterColorHandlersEvent.Block event) {
        final BlockColor dustBlockColorHandler = (state, level, pos, tintIndex) -> {
            Block block = state.getBlock();
            if (block instanceof DustLayerBlock dustBlock) {
                return dustBlock.getColor();
            }
            return -1;
        };

        for (Block block : ModBlocks.DYNAMICALLY_REGISTERED_DUST_LAYERS.values()) {
            event.getBlockColors().register(dustBlockColorHandler, block);
        }
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        final ItemColor dustColorHandler = (stack, tintIndex) -> {
            Block block = Block.byItem(stack.getItem());

            if (block instanceof DustLayerBlock dustBlock) {
                return dustBlock.getColor();
            }
            return -1;
        };

        for (Block block : ModBlocks.DYNAMICALLY_REGISTERED_DUST_LAYERS.values()) {
            Item item = block.asItem();
            if (item != null) {
                event.getItemColors().register(dustColorHandler, item);
            }
        }
    }
}
