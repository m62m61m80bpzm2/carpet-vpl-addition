package carpetvpladdition.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class VersionedMixinPlugin implements IMixinConfigPlugin {

    private static boolean isAtLeast1_21_11 = false;

    @Override
    public void onLoad(String mixinPackage) {
        try {
            var minecraft = FabricLoader.getInstance().getModContainer("minecraft");
            if (minecraft.isPresent()) {
                String version = minecraft.get().getMetadata().getVersion().getFriendlyString();
                if (version.startsWith("1.21.11") || version.startsWith("1.21.")) {
                    String[] parts = version.split("\\.");
                    if (parts.length >= 3) {
                        int minor = Integer.parseInt(parts[2].replaceAll("[^0-9].*", ""));
                        isAtLeast1_21_11 = minor >= 11;
                    }
                }
                if (version.startsWith("26")) isAtLeast1_21_11 = true;
            }
        } catch (Exception e) {
            isAtLeast1_21_11 = false;
        }
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // 以下 mixin 依赖 1.21.11+ 专属 API，低版本跳过
        if (!isAtLeast1_21_11) {
            if (mixinClassName.equals("carpetvpladdition.mixin.RecipeManagerAccessor")) return false;
            if (mixinClassName.equals("carpetvpladdition.mixin.RecipeManagerMixin")) return false;
        }
        return true;
    }

    @Override
    public String getRefMapperConfig() { return null; }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() { return null; }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
