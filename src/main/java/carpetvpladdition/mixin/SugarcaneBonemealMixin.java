package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;

@Mixin(SugarCaneBlock.class)
@Implements(@Interface(iface = BonemealableBlock.class, prefix = "cane$"))
public abstract class SugarcaneBonemealMixin {

    /**
     * 从 pos 向上找到甘蔗柱的顶端（pos 本身或更高的甘蔗）。
     */
    private static BlockPos findTop(LevelReader level, BlockPos pos) {
        BlockPos top = pos;
        while (level.getBlockState(top.above()).is(Blocks.SUGAR_CANE)) {
            top = top.above();
        }
        return top;
    }

    /**
     * 计算整根甘蔗柱的总高度：从顶端向下数到第一个非甘蔗方块（泥土等）为止。
     * 无论对柱内哪一格调用，结果都是整根柱的高度。
     */
    private static int totalHeight(LevelReader level, BlockPos pos) {
        int height = 1;
        BlockPos below = findTop(level, pos).below();
        while (level.getBlockState(below).is(Blocks.SUGAR_CANE)) {
            height++;
            below = below.below();
        }
        return height;
    }

    public boolean cane$isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        if (!CarpetVPLAdditionSettings.bedrockSugarcaneBonemeal) return false;
        // 原版甘蔗最大 3 格：未到 3 格且顶端上方是空气才可催熟（对柱内任意一格用骨粉均有效）
        return totalHeight(level, pos) < 3 && level.getBlockState(findTop(level, pos).above()).isAir();
    }

    public boolean cane$isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return CarpetVPLAdditionSettings.bedrockSugarcaneBonemeal;
    }

    public void cane$performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (!CarpetVPLAdditionSettings.bedrockSugarcaneBonemeal) return;
        BlockPos top = findTop(level, pos);
        if (!level.getBlockState(top.above()).isAir()) return;
        if (totalHeight(level, pos) >= 3) return;
        level.setBlockAndUpdate(top.above(), Blocks.SUGAR_CANE.defaultBlockState());
    }
}
