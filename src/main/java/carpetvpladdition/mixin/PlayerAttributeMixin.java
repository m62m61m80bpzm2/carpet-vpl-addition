package carpetvpladdition.mixin;

import carpetvpladdition.CarpetVPLAdditionExtension;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class PlayerAttributeMixin {
    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        CarpetVPLAdditionExtension.applyAttributes(self);
    }

    @Inject(method = "restoreFrom", at = @At("RETURN"))
    private void onRespawn(CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        CarpetVPLAdditionExtension.applyAttributes(self);
    }
}
