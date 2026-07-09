package carpetvpladdition.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

/**
 * 重新引入刷线机（MC-129055 / MC-59471）。
 *
 * Mojang 在 24w33a / 1.21.2 中修复了 tripwire 复制漏洞，在
 * TripWireHookBlock.calculateState() 里做了两件事：
 *   1) fix area 循环加类型守卫：
 *        if (blockState6.is(Blocks.TRIPWIRE) || blockState6.is(Blocks.TRIPWIRE_HOOK))
 *      使 setBlock 只在目标仍是线/线钩时执行；被水冲掉的位置是空气/水，故不被重新放置。
 *   2) fix area 循环外层条件 if (bl3 != bl5)：bl3 是线钩“旧的”ATTACHED 状态，
 *      bl5 是本次扫描算出的“新的”ATTACHED。第一次 bl3=false≠bl5=true 会进入，
 *      但刷线机一旦运作，线钩 ATTACHED 已为 true，之后每一次 bl3==bl5，循环被跳过，
 *      被冲掉的线不再被放回 → 表现为“刷一次就坏”。
 *
 * 本 mixin 在规则 stringDupe 开启时：
 *   - 绕过 1) 的类型守卫（ordinal 3 的 is(TRIPWIRE_HOOK) 检查），让被冲掉的位置也能被 setBlock 成线；
 *   - 绕过 2) 的 bl3 != bl5：让读取 ATTACHED 时恒为 false，使 bl3 恒不等于 bl5（当 bl5 为真时），
 *     fix area 每次都执行，重新放置被水冲掉的线。
 */
@Mixin(TripWireHookBlock.class)
public abstract class TripwireHookBlockStringDupeMixin {

    // 1) 绕过 fix area 类型守卫：让 setBlock 也能作用于被水冲掉的（空气/水）位置
    @WrapOperation(
        method = "calculateState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z",
            ordinal = 3
        ),
        require = 0
    )
    private static boolean onTripwireHookCheck(BlockState instance, Block block, Operation<Boolean> original) {
        if (CarpetVPLAdditionSettings.stringDupe) {
            return true;
        }
        return original.call(instance, block);
    }

    // 2) 绕过 bl3 != bl5：让线钩“旧的”ATTACHED 状态在 stringDupe 时恒为 false，
    //    使 fix area 循环在每次 calculateState 都执行，重新放置被冲掉的线。
    @WrapOperation(
        method = "calculateState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;getOptionalValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/util/Optional;",
            ordinal = 1
        ),
        require = 0
    )
    private static Optional<?> onAttachedRead(BlockState instance, Property<?> property, Operation<Optional<?>> original) {
        if (CarpetVPLAdditionSettings.stringDupe && property == TripWireHookBlock.ATTACHED) {
            return Optional.of(false);
        }
        return original.call(instance, property);
    }
}
