package org.ctrwaz.repother.reg;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.ctrwaz.repother.Repother;
import org.ctrwaz.repother.entity.DustLayerFallingBlockEntity;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Repother.MODID);

    public static final RegistryObject<EntityType<DustLayerFallingBlockEntity>> DUST_LAYER_FALLING_ENTITY =
            ENTITIES.register("dust_layer_falling_block",
                    () -> EntityType.Builder.<DustLayerFallingBlockEntity>of(DustLayerFallingBlockEntity::new, MobCategory.MISC)
                            .sized(0.98F, 0.98F) // 尺寸和原版FallingBlockEntity一致
                            .clientTrackingRange(10)
                            .updateInterval(20)
                            .build("dust_layer_falling_block")
            );
}
