package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonBaseBlock.class)
public abstract class PushableFurnaceMixin {
    @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
    private static void allowFurnacePush(BlockState state, Level level, BlockPos pos, Direction direction, boolean allowDestroy, Direction pistonDirection, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetVPLAdditionSettings.bedrockPushableFurnace && state.getBlock() instanceof AbstractFurnaceBlock) {
            cir.setReturnValue(true);
        }
    }
}
