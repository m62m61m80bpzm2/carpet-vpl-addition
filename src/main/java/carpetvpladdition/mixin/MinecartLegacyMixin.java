package carpetvpladdition.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractMinecart.class)
public class MinecartLegacyMixin {

    @WrapOperation(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;computeSpeed()V")
    )
    private void wrapComputeSpeed(Entity instance, Operation<Void> original) {
        if (!CarpetVPLAdditionSettings.legacyMinecartMovement) {
            original.call(instance);
        }
    }

    @WrapOperation(
        method = "move",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/minecart/AbstractMinecart;useExperimentalMovement(Lnet/minecraft/world/level/Level;)Z")
    )
    private boolean wrapUseExperimentalMovement(Level level, Operation<Boolean> original) {
        if (CarpetVPLAdditionSettings.legacyMinecartMovement) {
            return false;
        }
        return original.call(level);
    }
}
