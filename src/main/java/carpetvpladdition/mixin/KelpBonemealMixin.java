package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrowingPlantHeadBlock.class)
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
}
