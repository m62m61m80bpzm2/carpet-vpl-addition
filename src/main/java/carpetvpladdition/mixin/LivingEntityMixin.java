package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class LivingEntityMixin {
    @ModifyReturnValue(method = "getMaxAirSupply", at = @At("RETURN"))
    private int modifyMaxAirSupply(int original) {
        if (!((Object) this instanceof Player)) return original;
        try {
            return Integer.parseInt(CarpetVPLAdditionSettings.maxAir);
        } catch (NumberFormatException e) {
            return original;
        }
    }
}
