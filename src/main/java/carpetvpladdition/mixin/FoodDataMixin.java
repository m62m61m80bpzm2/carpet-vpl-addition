package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {
    @ModifyArg(
        method = "add",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(III)I"),
        index = 2
    )
    private int modifyFoodLevelCap(int original) {
        try {
            return Integer.parseInt(CarpetVPLAdditionSettings.maxSaturation);
        } catch (NumberFormatException e) {
            return 20;
        }
    }
}
