package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.KelpBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KelpBlock.class)
public class KelpBonemealGrowthMixin {

    @Inject(method = "getBlocksToGrowWhenBonemealed", at = @At("RETURN"), cancellable = true)
    private void onGetBlocksToGrowWhenBonemealed(RandomSource random, CallbackInfoReturnable<Integer> cir) {
        if (CarpetVPLAdditionSettings.bedrockKelpBonemeal) {
            cir.setReturnValue(25);
        }
    }
}
