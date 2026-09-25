package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 甘蔗可被骨粉催熟（基岩版行为；b1.16.1.1 重写，规则 bedrockSugarcaneBonemeal）。
 *
 * 旧实现用 @Implements(@Interface(iface = BonemealableBlock.class, prefix = "cane$")) 给
 * SugarCaneBlock 补上骨粉接口。26.3 给 BonemealableBlock 的三个方法统一加了
 * BonemealSource 参数，接口里已不存在旧签名的 isValidBonemealTarget(LevelReader,BlockPos,BlockState)，
 * Mixin 在应用阶段就会报
 * "isValidBonemealTarget does not exist in target interface BonemealableBlock" 并导致服务端启动失败
 * （26.3 实测复现，且这类失败在按 26.1.2 编译时完全不报错，只能靠真起服务端发现）。
 *
 * 现与 CactusBonemealMixin 采用同一方案：注入 BoneMealItem.growCrop(ItemStack, Level, BlockPos)Z
 * 的 HEAD。该描述符 26.1/26.2/26.3 三版一致，玩家手点与发射器共用，故一个 jar 覆盖三版。
 * 行为照旧：对柱内任意一格用骨粉都按整根柱计算高度，不足 3 格且顶端上方为空则长一节。
 */
@Mixin(BoneMealItem.class)
public abstract class SugarcaneBonemealMixin {

    @Inject(
        method = "growCrop(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void vplGrowSugarcane(ItemStack boneMeal, Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!CarpetVPLAdditionSettings.bedrockSugarcaneBonemeal || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!serverLevel.getBlockState(pos).is(Blocks.SUGAR_CANE)) {
            return; // 非甘蔗，交还原版逻辑
        }
        BlockPos top = findTop(serverLevel, pos);
        if (!serverLevel.getBlockState(top.above()).isAir() || totalHeight(serverLevel, top) >= 3) {
            return;
        }
        serverLevel.setBlockAndUpdate(top.above(), Blocks.SUGAR_CANE.defaultBlockState());
        boneMeal.shrink(1);
        BoneMealItem.addGrowthParticles(serverLevel, top, 1);
        cir.setReturnValue(true);
    }

    /** 从给定位向上找到甘蔗柱顶端 */
    private static BlockPos findTop(LevelReader level, BlockPos pos) {
        BlockPos top = pos;
        while (level.getBlockState(top.above()).is(Blocks.SUGAR_CANE)) {
            top = top.above();
        }
        return top;
    }

    /** 整根甘蔗柱的高度（从顶端向下数到第一个非甘蔗方块） */
    private static int totalHeight(LevelReader level, BlockPos top) {
        int height = 1;
        BlockPos below = top.below();
        while (level.getBlockState(below).is(Blocks.SUGAR_CANE)) {
            height++;
            below = below.below();
        }
        return height;
    }
}
