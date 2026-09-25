package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 仙人掌可被骨粉催熟（b1.16.1.1 重写，规则 cactusBoneMeal）。
 *
 * 旧实现是 @Mixin(CactusBlock.class) implements BonemealableBlock 并 @Override 接口的三个方法。
 * 该写法在 26.3 失效：26.3 给 BonemealableBlock 的三个方法统一加了 BonemealSource 参数
 * （isValidBonemealTarget/isBonemealSuccess/performBonemeal 全部改描述符），
 * 旧签名方法在 26.3 上不再覆盖任何接口方法，规则会静默失效；
 * 而且这些方法带 @Override，源码按 26.3 编译时直接编译不过。
 *
 * 现改为注入 BoneMealItem.growCrop(ItemStack, Level, BlockPos)Z 的 HEAD ——
 * 该方法描述符在 26.1 / 26.2 / 26.3 三版完全一致，且玩家手持右键与发射器都走它，
 * 因此一个 jar 同时覆盖三个版本，无需版本门控。判定与生长逻辑保持原样：
 * 上方有空位且整根仙人掌不足 3 节时长高一节。
 */
@Mixin(BoneMealItem.class)
public abstract class CactusBonemealMixin {

    @Inject(
        method = "growCrop(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void vplGrowCactus(ItemStack boneMeal, Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!CarpetVPLAdditionSettings.cactusBoneMeal || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockState state = serverLevel.getBlockState(pos);
        if (!state.is(Blocks.CACTUS)) {
            return; // 非仙人掌，交还原版逻辑
        }
        BlockPos above = pos.above();
        if (!serverLevel.isEmptyBlock(above)) {
            return;
        }
        // 沿柱体向下数节数，满 3 节则不再催熟（与旧实现一致）
        int height = 1;
        while (serverLevel.getBlockState(pos.below(height)).is(Blocks.CACTUS)) {
            if (++height >= 3) {
                return;
            }
        }
        serverLevel.setBlockAndUpdate(above, ((CactusBlock) state.getBlock()).defaultBlockState());
        BlockState newState = state.setValue(CactusBlock.AGE, 0);
        serverLevel.setBlock(pos, newState, Block.UPDATE_ALL);
        serverLevel.neighborChanged(newState, above, state.getBlock(), null, false);
        boneMeal.shrink(1);
        BoneMealItem.addGrowthParticles(serverLevel, pos, 1);
        cir.setReturnValue(true);
    }
}
