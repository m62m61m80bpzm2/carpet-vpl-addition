package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMinecartLegacyMixin {

    @Inject(method = "computeSpeed", at = @At("HEAD"), cancellable = true)
    private void onComputeSpeed(CallbackInfo ci) {
        if (CarpetVPLAdditionSettings.legacyMinecartMovement && ((Entity) (Object) this) instanceof AbstractMinecart) {
            ci.cancel();
        }
    }
}
