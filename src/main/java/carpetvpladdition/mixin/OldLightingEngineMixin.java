package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelLightEngine.class)
public abstract class OldLightingEngineMixin {
    private static int lightUpdateCount = 0;
    private static final int MAX_LIGHT_UPDATES = 2000;

    @Inject(method = "checkBlock", at = @At("HEAD"), cancellable = true)
    private void onCheckBlock(BlockPos pos, CallbackInfo ci) {
        if (!CarpetVPLAdditionSettings.oldLightingEngine) return;
        lightUpdateCount++;
        if (lightUpdateCount > MAX_LIGHT_UPDATES) {
            ci.cancel();
        }
    }

    @Inject(method = "runLightUpdates", at = @At("HEAD"))
    private void onRunLightUpdates(CallbackInfoReturnable<Integer> cir) {
        if (CarpetVPLAdditionSettings.oldLightingEngine) {
            lightUpdateCount = 0;
        }
    }
}
