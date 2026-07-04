package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.projectile.ThrownTrident;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownTrident.class)
public abstract class TridentVoidReturnMixin {
    @Shadow
    private static EntityDataAccessor<Byte> ID_LOYALTY;

    @Shadow
    protected abstract boolean isAcceptibleReturnOwner();

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickTail(CallbackInfo ci) {
        if (!CarpetVPLAdditionSettings.tridentVoidReturn) return;

        ThrownTrident self = (ThrownTrident) (Object) this;
        if (self.getY() >= self.level().getMinY() - 10) return;

        int loyalty = self.getEntityData().get(ID_LOYALTY);
        if (loyalty <= 0) return;

        if (!isAcceptibleReturnOwner()) return;

        self.setNoPhysics(true);
        self.setDeltaMovement(
            self.getDeltaMovement().x,
            self.getDeltaMovement().y + 0.1,
            self.getDeltaMovement().z
        );
    }
}
