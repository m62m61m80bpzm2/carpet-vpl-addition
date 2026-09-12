package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 青金石岩浆源（b1.14.5.1 新增，规则 lavaSourceOnLapis）。
 *
 * 复刻无限水机制：流动岩浆的水平相邻 4 个方向中至少 2 个方向是岩浆源，
 * 且其正下方是青金石块时，在下一次流体计划 tick 转化为岩浆源方块。
 * 省去反复倾倒岩浆桶，方便建造岩浆田/刷石机。
 *
 * 注入 FlowingFluid.tick HEAD（所有流动流体计划 tick 的统一入口）：
 * 满足条件时替换为源方块并取消本次 spread；源方块放置时 LiquidBlock.onPlace
 * 会 scheduleTick，之后由原版流体机制自然接管流动。源岩浆不参与转换。
 */
@Mixin(FlowingFluid.class)
public abstract class FlowingFluidTickMixin {

    @Inject(
        method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void lavaSourceOnLapis(ServerLevel level, BlockPos pos, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
        if (!CarpetVPLAdditionSettings.lavaSourceOnLapis) {
            return;
        }
        // 仅处理流动岩浆（非源）；水源/岩浆源不干预
        if (!fluidState.is(FluidTags.LAVA) || fluidState.isSource()) {
            return;
        }
        // 正下方必须是青金石块
        if (!level.getBlockState(pos.below()).is(Blocks.LAPIS_BLOCK)) {
            return;
        }
        // 无限水判定：水平相邻 4 个方向中至少 2 个方向是岩浆源（只统计岩浆源，水源不算）
        int lavaSourceNeighbors = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            FluidState neighbor = level.getFluidState(pos.relative(direction));
            if (neighbor.is(FluidTags.LAVA) && neighbor.isSource()) {
                lavaSourceNeighbors++;
                if (lavaSourceNeighbors >= 2) {
                    break;
                }
            }
        }
        if (lavaSourceNeighbors < 2) {
            return;
        }
        // 转换为岩浆源方块（LEVEL=0 即源）；取消本次 spread，转换后的源
        // 由 onPlace 重新排程 tick，之后完全按原版源岩浆行为流动
        level.setBlock(pos, Blocks.LAVA.defaultBlockState(), 3);
        ci.cancel();
    }
}
