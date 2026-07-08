package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CactusBlock.class)
public abstract class CactusBonemealMixin implements BonemealableBlock {

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        if (!CarpetVPLAdditionSettings.cactusBoneMeal) {
            return false;
        }
        // 只有当上方有空间且总高度 < 3 时可催熟
        BlockPos above = blockPos.above();
        if (!levelReader.isEmptyBlock(above)) {
            return false;
        }
        int height = 1;
        while (levelReader.getBlockState(blockPos.below(height)).is((CactusBlock) (Object) this)) {
            if (++height >= 3) {
                return false;  // 高度已达 3 节，无法再长
            }
        }
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return true;  // 100% 成功
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        // 强制长高一节（无视 AGE）
        BlockPos above = blockPos.above();
        if (serverLevel.isEmptyBlock(above)) {
            serverLevel.setBlockAndUpdate(above, ((CactusBlock) (Object) this).defaultBlockState());
            BlockState newState = blockState.setValue(CactusBlock.AGE, 0);
            serverLevel.setBlock(blockPos, newState, 260);
            serverLevel.neighborChanged(newState, above, (CactusBlock) (Object) this, null, false);
        }
    }
}
