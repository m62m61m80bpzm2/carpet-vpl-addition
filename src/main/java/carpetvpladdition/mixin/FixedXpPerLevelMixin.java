package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

@Mixin(Player.class)
public abstract class FixedXpPerLevelMixin {
    @ModifyReturnValue(method = "getXpNeededForNextLevel", at = @At("RETURN"))
    private int modifyXpNeeded(int original) {
        if (CarpetVPLAdditionSettings.fixedXpPerLevel) {
            return 62;
        }
        return original;
    }
}
