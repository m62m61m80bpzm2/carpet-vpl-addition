package carpetvpladdition.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 修复刷线机"用一次就坏"的问题。
 * TripWireBlock.updateSource() 在扫描线钩时，
 * 遇到非线方块（如被水冲掉的缺口）会 break 停止扫描，
 * 导致第二次刷线时找不到线钩，calculateState 不被触发。
 * 本 mixin 在 stringDupe 开启时绕过该 break，使扫描能跨越缺口找到线钩。
 */
@Mixin(TripWireBlock.class)
public abstract class TripWireBlockStringDupeMixin {

    @WrapOperation(
        method = "updateSource",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z",
            ordinal = 1
        )
    )
    private boolean onConnectivityCheck(BlockState instance, Block block, Operation<Boolean> original) {
        if (CarpetVPLAdditionSettings.stringDupe) {
            return true;
        }
        return original.call(instance, block);
    }
}
