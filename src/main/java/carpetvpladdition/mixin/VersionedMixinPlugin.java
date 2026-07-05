package carpetvpladdition.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * 版本门控 Mixin 插件。
 *
 * 根据 MC 版本动态启用/禁用某些 mixin，确保跨版本兼容。
 * 已通过反编译 1.21.0 ~ 26.2 共 15 个版本的源码确认差异。
 */
public class VersionedMixinPlugin implements IMixinConfigPlugin {

    private static boolean isAtLeast1_21_2 = false;
    private static boolean isAtLeast1_21_6 = false;
    private static boolean isAtLeast1_21_11 = false;

    @Override
    public void onLoad(String mixinPackage) {
        try {
            var minecraft = FabricLoader.getInstance().getModContainer("minecraft");
            if (minecraft.isPresent()) {
                String version = minecraft.get().getMetadata().getVersion().getFriendlyString();
                if (version.startsWith("1.21.") || version.startsWith("26")) {
                    if (version.startsWith("26")) {
                        // 26.x (Spring to Life) 包含所有 1.21.x 的 API
                        isAtLeast1_21_2 = true;
                        isAtLeast1_21_6 = true;
                        isAtLeast1_21_11 = true;
                    } else {
                        String[] parts = version.split("\\.");
                        if (parts.length >= 3) {
                            int minor = Integer.parseInt(parts[2].replaceAll("[^0-9].*", ""));
                            isAtLeast1_21_2 = minor >= 2;
                            isAtLeast1_21_6 = minor >= 6;
                            isAtLeast1_21_11 = minor >= 11;
                        }
                    }
                }
            }
        } catch (Exception e) {
            isAtLeast1_21_2 = false;
            isAtLeast1_21_6 = false;
            isAtLeast1_21_11 = false;
        }
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // === 1.21.11+ 专属 ===
        // RecipeManagerAccessor 和 RecipeManagerMixin 需要 1.21.11+ 的 RecipeManager API
        if (!isAtLeast1_21_11) {
            if (mixinClassName.equals("carpetvpladdition.mixin.RecipeManagerAccessor")) return false;
            if (mixinClassName.equals("carpetvpladdition.mixin.RecipeManagerMixin")) return false;
        }

        // === 1.21.6+ 专属 ===
        // ValueOutput 接口在 1.21.6 才引入，CompoundTagValueOutput 实现了它
        // VillagerReincarnationMixin 依赖 CompoundTagValueOutput
        if (!isAtLeast1_21_6) {
            if (mixinClassName.equals("carpetvpladdition.mixin.VillagerReincarnationMixin")) return false;
        }

        // === 1.21.2+ 专属 ===
        // EntitySpawnReason 在 1.21.0-1.21.1 中不存在（使用 MobSpawnType）
        // 以下 mixin 使用了 EntityType.create(Level, EntitySpawnReason) 签名
        if (!isAtLeast1_21_2) {
            if (mixinClassName.equals("carpetvpladdition.mixin.VillagerGolemMixin")) return false;
            if (mixinClassName.equals("carpetvpladdition.mixin.PortalZombiePigmanMixin")) return false;
        }

        // 刷线机保护判断在 1.21.2+ 才存在，低于此版本不需此 mixin（漏洞原生存在）
        if (!isAtLeast1_21_2) {
            if (mixinClassName.equals("carpetvpladdition.mixin.TripwireHookBlockStringDupeMixin")) return false;
            // TripWireBlock.updateSource 的保护性 break 也是 1.21.2+ 修复的一部分
            if (mixinClassName.equals("carpetvpladdition.mixin.TripWireBlockStringDupeMixin")) return false;
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
