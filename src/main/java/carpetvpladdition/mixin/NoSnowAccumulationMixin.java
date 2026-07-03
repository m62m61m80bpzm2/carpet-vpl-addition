package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class NoSnowAccumulationMixin {

    @Inject(method = "tickPrecipitation", at = @At("HEAD"), cancellable = true, require = 0)
    private void onTickPrecipitation(BlockPos pos, CallbackInfo ci) {
        if (CarpetVPLAdditionSettings.disableSnow) {
            ci.cancel();
        }
    }
}
