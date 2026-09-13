package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * 骨粉催熟紫水晶（b1.15.1.1 新增，规则 amethystBoneMeal）。
 *
 * 注入 BoneMealItem.growCrop（static，玩家手持右键与发射器发射骨粉的公共路径）：
 * 目标是紫水晶芽（小/中/大芽）时推进一个生长阶段，晶簇已满不再处理。
 * 其他方块完全不干预，走原版逻辑——满足"只改水晶，其他不管"。
 * 发射器路径无需额外处理：原版发射器已注册骨粉行为并调用 growCrop。
 */
@Mixin(BoneMealItem.class)
public abstract class AmethystBoneMealMixin {

    /** 紫水晶芽的下一生长阶段映射 */
    private static final Map<Block, Block> AMETHYST_NEXT_STAGE = Map.of(
        Blocks.SMALL_AMETHYST_BUD, Blocks.MEDIUM_AMETHYST_BUD,
        Blocks.MEDIUM_AMETHYST_BUD, Blocks.LARGE_AMETHYST_BUD,
        Blocks.LARGE_AMETHYST_BUD, Blocks.AMETHYST_CLUSTER
    );

    @Inject(
        method = "growCrop(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void vplGrowAmethyst(ItemStack boneMeal, Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!CarpetVPLAdditionSettings.amethystBoneMeal) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        Block nextStage = AMETHYST_NEXT_STAGE.get(state.getBlock());
        if (nextStage == null) {
            return; // 非紫水晶芽，交还原版逻辑
        }
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            boneMeal.shrink(1);
            // withPropertiesOf 复制 FACING/WATERLOGGED 等共有属性，保持朝向
            serverLevel.setBlock(pos, nextStage.withPropertiesOf(state), Block.UPDATE_ALL);
            serverLevel.levelEvent(1505, pos, 15); // 骨粉成功粒子
        }
        cir.setReturnValue(true);
    }
}
