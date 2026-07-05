package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * 修改最大保湿度。
 *
 * 性能修复：使用缓存的 maxSaturationCached，避免每次吃东西时 parseInt。
 */
@Mixin(FoodData.class)
public abstract class FoodDataMixin {
    @ModifyArg(
        method = "add",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(III)I"),
        index = 2
    )
    private int modifyFoodLevelCap(int original) {
        return CarpetVPLAdditionSettings.maxSaturationCached;
    }
}
