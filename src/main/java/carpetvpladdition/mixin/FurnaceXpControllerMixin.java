package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 熔炉经验倍率。
 *
 * 性能修复：使用缓存的 furnaceXpMultiplierCached，避免每次烧炼产出时 parseFloat。
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class FurnaceXpControllerMixin {
    @ModifyVariable(
        method = "createExperience",
        at = @At("HEAD"),
        index = 3,
        argsOnly = true
    )
    private static float multiplyXpPerItem(float xpPerItem) {
        float multiplier = CarpetVPLAdditionSettings.furnaceXpMultiplierCached;
        if (multiplier != 1.0F) {
            return xpPerItem * multiplier;
        }
        return xpPerItem;
    }
}
