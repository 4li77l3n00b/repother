package org.ctrwaz.repother.mixin;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {
    private static final Logger LOGGER = LogManager.getLogger("ConditionalMixinPlugin");

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return "";
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (Objects.equals(mixinClassName, "org.ctrwaz.repother.mixin.DustPrecipitationMixin")) {
            boolean exists = isClassExists("net.mehvahdjukaar.supplementaries.Supplementaries");
            LOGGER.info("[critical]Checking class net.mehvahdjukaar.supplementaries.Supplementaries: exists={}", exists);
            return exists;
        } else {
            return true;
        }
    }

    private static boolean isClassExists(String className) {
        try {
            Class.forName(className, false, MixinPlugin.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
