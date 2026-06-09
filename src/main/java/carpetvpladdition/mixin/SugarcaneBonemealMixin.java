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

    public boolean cane$isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        if (!CarpetVPLAdditionSettings.bedrockSugarcaneBonemeal) return false;
        if (!level.getBlockState(pos.above()).isAir()) return false;
        int height = 1;
        BlockPos below = pos.below();
        while (level.getBlockState(below).is(Blocks.SUGAR_CANE) && height < 3) {
            height++;
            below = below.below();
        }
        return height < 3;
    }

    public boolean cane$isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return CarpetVPLAdditionSettings.bedrockSugarcaneBonemeal;
    }

    public void cane$performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (!CarpetVPLAdditionSettings.bedrockSugarcaneBonemeal) return;
        if (!level.getBlockState(pos.above()).isAir()) return;
        level.setBlockAndUpdate(pos.above(), Blocks.SUGAR_CANE.defaultBlockState());
    }
}
