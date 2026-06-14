package carpetvpladdition.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KelpBlock.class)
public class KelpBonemealMixin {

    @Inject(method = "isValidBonemealTarget", at = @At("HEAD"), cancellable = true)
    private void onIsValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetVPLAdditionSettings.bedrockKelpBonemeal) {
            cir.setReturnValue(level.getBlockState(pos.above()).canBeReplaced());
        }
    }

    @Inject(method = "isBonemealSuccess", at = @At("HEAD"), cancellable = true)
    private void onIsBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetVPLAdditionSettings.bedrockKelpBonemeal) {
            cir.setReturnValue(true);
        }
    }

    @ModifyReturnValue(method = "getBlocksToGrowWhenBonemealed", at = @At("RETURN"))
    private int onGetBlocksToGrowWhenBonemealed(int original, RandomSource random) {
        if (CarpetVPLAdditionSettings.bedrockKelpBonemeal) {
            return 25;
        }
        return original;
    }
}
