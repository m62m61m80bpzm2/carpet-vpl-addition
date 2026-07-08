package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CactusBlock.class)
public abstract class CactusGrowthMixin {

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void acceleratedCactusGrowth(
            BlockState blockState,
            ServerLevel serverLevel,
            BlockPos blockPos,
            RandomSource randomSource,
            CallbackInfo ci
    ) {
        float multiplier = CarpetVPLAdditionSettings.cactusGrowthMultiplierCached;
        if (multiplier <= 1.0f) {
            return;  // 默认 1.0 = 原版逻辑继续
        }

        // 加速模式：复制原版 randomTick 的逻辑，按倍率重写概率
        BlockPos blockPos2 = blockPos.above();
        if (!serverLevel.isEmptyBlock(blockPos2)) {
            ci.cancel();
            return;
        }

        int i = 1;
        int j = blockState.getValue(CactusBlock.AGE);

        while (serverLevel.getBlockState(blockPos.below(i)).is((CactusBlock) (Object) this)) {
            if (++i == 3 && j == 15) {
                ci.cancel();
                return;
            }
        }

        if (j == 8 && canCactusSurvive(this.asCactus(), this.asCactus().defaultBlockState(), serverLevel, blockPos.above())) {
            double baseChance = i >= 3 ? 0.25 : 0.1;
            // 加速：概率 = baseChance * multiplier，但不超过 1.0
            double chance = Math.min(1.0, baseChance * multiplier);
            if (randomSource.nextDouble() <= chance) {
                serverLevel.setBlockAndUpdate(blockPos2, Blocks.CACTUS_FLOWER.defaultBlockState());
            }
        } else if (j == 15 && i < 3) {
            serverLevel.setBlockAndUpdate(blockPos2, this.asCactus().defaultBlockState());
            BlockState blockState2 = blockState.setValue(CactusBlock.AGE, 0);
            serverLevel.setBlock(blockPos, blockState2, 260);
            serverLevel.neighborChanged(blockState2, blockPos2, this.asCactus(), null, false);
        }

        if (j < 15) {
            // AGE 增加的速率也按 multiplier 提升
            int ageIncrease = Math.min(15, j + Math.max(1, (int) Math.floor(multiplier)));
            if (ageIncrease > j) {
                serverLevel.setBlock(blockPos, blockState.setValue(CactusBlock.AGE, ageIncrease), 260);
            }
        }

        ci.cancel();
    }

    // 辅助：将 this 转为 CactusBlock
    private CactusBlock asCactus() {
        return (CactusBlock) (Object) this;
    }

    // 辅助：调用 CactusBlock.canSurvive（protected）
    private static boolean canCactusSurvive(CactusBlock block, BlockState state, ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockState blockState2 = level.getBlockState(pos.relative(direction));
            if (blockState2.isSolid() || level.getFluidState(pos.relative(direction)).is(FluidTags.LAVA)) {
                return false;
            }
        }
        BlockState below = level.getBlockState(pos.below());
        return (below.is(Blocks.CACTUS) || below.is(BlockTags.SAND)) && !level.getBlockState(pos.above()).liquid();
    }
}
