package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CactusBlock.class)
public abstract class CactusGrowthMixin {

    @Shadow
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) { throw new AssertionError(); }

    private CactusBlock asCactus() {
        return (CactusBlock) (Object) this;
    }

    /**
     * 加速仙人掌生长：倍率作用于随机刻的发生概率与 AGE 增长速率。
     * 倍率 1.0 = 原版逻辑；倍率 > 1.0 = 加速。
     */
    @Overwrite
    protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        float multiplier = CarpetVPLAdditionSettings.cactusGrowthMultiplierCached;
        if (multiplier <= 1.0f) {
            // 1.0 = 原版逻辑，inline 复制以避免无限递归
            BlockPos above = blockPos.above();
            if (!serverLevel.isEmptyBlock(above)) {
                return;
            }
            int i = 1;
            int age = blockState.getValue(CactusBlock.AGE);
            while (serverLevel.getBlockState(blockPos.below(i)).is((CactusBlock) (Object) this)) {
                if (++i == 3 && age == 15) {
                    return;
                }
            }
            if (age == 8 && this.canSurvive(this.asCactus().defaultBlockState(), serverLevel, blockPos.above())) {
                double d = i >= 3 ? 0.25 : 0.1;
                if (randomSource.nextDouble() <= d) {
                    serverLevel.setBlockAndUpdate(above, Blocks.CACTUS_FLOWER.defaultBlockState());
                }
            } else if (age == 15 && i < 3) {
                serverLevel.setBlockAndUpdate(above, this.asCactus().defaultBlockState());
                BlockState newState = blockState.setValue(CactusBlock.AGE, 0);
                serverLevel.setBlock(blockPos, newState, 260);
                serverLevel.neighborChanged(newState, above, (CactusBlock) (Object) this, null, false);
            }
            if (age < 15) {
                serverLevel.setBlock(blockPos, blockState.setValue(CactusBlock.AGE, age + 1), 260);
            }
            return;
        }

        // 加速模式
        BlockPos above = blockPos.above();
        if (!serverLevel.isEmptyBlock(above)) {
            return;
        }
        int i = 1;
        int age = blockState.getValue(CactusBlock.AGE);
        while (serverLevel.getBlockState(blockPos.below(i)).is((CactusBlock) (Object) this)) {
            if (++i == 3 && age == 15) {
                return;
            }
        }
        if (age == 8 && this.canSurvive(this.asCactus().defaultBlockState(), serverLevel, blockPos.above())) {
            double baseChance = i >= 3 ? 0.25 : 0.1;
            double chance = Math.min(1.0, baseChance * multiplier);
            if (randomSource.nextDouble() <= chance) {
                serverLevel.setBlockAndUpdate(above, Blocks.CACTUS_FLOWER.defaultBlockState());
            }
        } else if (age == 15 && i < 3) {
            serverLevel.setBlockAndUpdate(above, this.asCactus().defaultBlockState());
            BlockState newState = blockState.setValue(CactusBlock.AGE, 0);
            serverLevel.setBlock(blockPos, newState, 260);
            serverLevel.neighborChanged(newState, above, (CactusBlock) (Object) this, null, false);
        }
        if (age < 15) {
            int ageIncrease = Math.min(15, age + Math.max(1, (int) Math.floor(multiplier)));
            if (ageIncrease > age) {
                serverLevel.setBlock(blockPos, blockState.setValue(CactusBlock.AGE, ageIncrease), 260);
            }
        }
    }
}
