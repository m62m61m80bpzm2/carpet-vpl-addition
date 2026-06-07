package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {
    @ModifyArg(
        method = "eat",
        at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"),
        index = 1
    )
    private int modifyFoodLevelCap(int original) {
        try {
            return Integer.parseInt(CarpetVPLAdditionSettings.maxSaturation);
        } catch (NumberFormatException e) {
            return 20;
        }
    }
}
