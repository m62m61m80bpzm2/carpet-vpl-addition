package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownTrident.class)
public abstract class TridentContinuousDamageMixin {
    @Shadow private boolean dealtDamage;

    @Redirect(
        method = "findHitEntity",
        at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/projectile/ThrownTrident;dealtDamage:Z")
    )
    private boolean redirectDealtDamage(ThrownTrident instance) {
        if (CarpetVPLAdditionSettings.tridentContinuousDamage) {
            return false;
        }
        return this.dealtDamage;
    }

    @Inject(method = "onHitEntity", at = @At("TAIL"))
    private void onHitEntityTail(EntityHitResult result, CallbackInfo ci) {
        if (CarpetVPLAdditionSettings.tridentContinuousDamage) {
            this.dealtDamage = false;
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickHead(CallbackInfo ci) {
        if (CarpetVPLAdditionSettings.tridentContinuousDamage) {
            this.dealtDamage = false;
        }
    }
}
