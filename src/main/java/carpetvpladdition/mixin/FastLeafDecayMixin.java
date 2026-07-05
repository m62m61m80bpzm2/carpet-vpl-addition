package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.RandomSource;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * 快速树叶腐烂。
 *
 * 原代码将 delay 硬改为 0，导致砍大树时几百片叶子同时排入下一 tick，
 * 全部 scheduleTick + 方块移除 + 掉落物 + 邻居更新挤在一个 tick 里，
 * 造成 MSPT 瞬间飙升几十毫秒、风扇狂转。
 *
 * 修复：改为 delay=1 仍比原版快得多，但能让 tick 负载分散。
 * 仅在原 delay > 1 时覆盖（避免无意义覆盖）。
 */
@Mixin(LeavesBlock.class)
public abstract class FastLeafDecayMixin {
    @ModifyArg(
        method = "updateShape",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ScheduledTickAccess;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;I)V"),
        index = 2
    )
    private int modifyTickDelay(int delay) {
        if (CarpetVPLAdditionSettings.fastLeafDecay && delay > 1) {
            return 1;
        }
        return delay;
    }
}
