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
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(CactusBlock.class)
public abstract class CactusGrowthMixin {

    /**
     * 加速仙人掌生长：倍率作用于随机刻的发生概率与 AGE 增长速率。
     * 倍率 1.0 = 原版逻辑；倍率 > 1.0 = 加速。
     */
    @Overwrite
    protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        float multiplier = CarpetVPLAdditionSettings.cactusGrowthMultiplierCached;
        if (multiplier <= 1.0f) {
            // 1.0 = 原版逻辑，inline 复制以避免无限递归
            BlockPos blockPos2 = blockPos.above();
            if (!serverLevel.isEmptyBlock(blockPos2)) {
                return;
            }
            int i = 1;
            int j = blockState.getValue(CactusBlock.AGE);
            while (serverLevel.getBlockState(blockPos.below(i)).is((CactusBlock) (Object) this)) {
                if (++i == 3 && j == 15) {
                    return;
                }
            }
            if (j == 8 && this.canCactusSurvive(this.asCactus().defaultBlockState(), serverLevel, blockPos.above())) {
                double d = i >= 3 ? 0.25 : 0.1;
                if (randomSource.nextDouble() <= d) {
                    serverLevel.setBlockAndUpdate(blockPos2, Blocks.CACTUS_FLOWER.defaultBlockState());
                }
            } else if (j == 15 && i < 3) {
                serverLevel.setBlockAndUpdate(blockPos2, this.asCactus().defaultBlockState());
                BlockState blockState2 = blockState.setValue(CactusBlock.AGE, 0);
                serverLevel.setBlock(blockPos, blockState2, 260);
                serverLevel.neighborChanged(blockState2, blockPos2, this.asCactus(), null, false);
            }
            if (j < 15) {
                serverLevel.setBlock(blockPos, blockState.setValue(CactusBlock.AGE, j + 1), 260);
            }
            return;
        }

        // 加速模式
        BlockPos blockPos2 = blockPos.above();
        if (!serverLevel.isEmptyBlock(blockPos2)) {
            return;
        }
        int i = 1;
        int j = blockState.getValue(CactusBlock.AGE);
        while (serverLevel.getBlockState(blockPos.below(i)).is((CactusBlock) (Object) this)) {
            if (++i == 3 && j == 15) {
                return;
            }
        }
        if (j == 8 && this.canCactusSurvive(this.asCactus().defaultBlockState(), serverLevel, blockPos.above())) {
            double baseChance = i >= 3 ? 0.25 : 0.1;
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
            int ageIncrease = Math.min(15, j + Math.max(1, (int) Math.floor(multiplier)));
            if (ageIncrease > j) {
                serverLevel.setBlock(blockPos, blockState.setValue(CactusBlock.AGE, ageIncrease), 260);
            }
        }
    }

    private CactusBlock asCactus() {
        return (CactusBlock) (Object) this;
    }

    private boolean canCactusSurvive(BlockState state, ServerLevel level, BlockPos pos) {
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
