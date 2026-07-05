package carpetvpladdition.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 重新引入刷线机（MC-129055 / MC-59471）
 * Mojang 在 24w33a / 1.21.2 中修复了 tripwire 复制漏洞，
 * 在 TripWireHookBlock.calculateState() 里加了保护判断：
 *   if (blockState.is(Blocks.TRIPWIRE) || blockState.is(Blocks.TRIPWIRE_HOOK))
 * 使 setBlock 只有在目标方块仍是线/线钩时才执行。
 * 本 mixin 在规则 stringDupe 开启时令该检查始终通过，还原旧行为。
 */
@Mixin(TripWireHookBlock.class)
public abstract class TripwireHookBlockStringDupeMixin {

    @WrapOperation(
        method = "calculateState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z",
            ordinal = 3
        ),
        require = 0
    )
    private static boolean onTripwireCheck(BlockState instance, Block block, Operation<Boolean> original) {
        if (CarpetVPLAdditionSettings.stringDupe) {
            return true;
        }
        return original.call(instance, block);
    }
}
