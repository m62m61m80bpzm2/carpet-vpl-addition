package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * 骨粉催熟紫水晶（b1.15.1.1 新增，b1.15.5 补齐母岩路径，规则 amethystBoneMeal）。
 *
 * 注入 BoneMealItem.growCrop（static，玩家手持右键与发射器发射骨粉的公共路径），
 * 覆盖两种点击目标：
 *
 * 1) 点到紫水晶芽（小/中/大芽）—— b1.15.1.1 原有行为，推进一个生长阶段。
 * 2) 点到紫水晶母岩（BUDDING_AMETHYST）—— b1.15.5 新增。
 *    这才是玩家实际会命中的目标：芽是贴在母岩表面的小体积方块，准星对着母岩那一面时
 *    右键拿到的 pos 就是母岩，原版母岩既不是 BonemealableBlock 也没有其它骨粉分支，
 *    growCrop 直接返回 false，故旧实现"看起来没效果"。
 *    现按原版 randomTick 的同一套朝向判定，依次尝试 6 个面，找到第一个可生长的位置
 *    就长出新芽或把已有芽推进一档；随机起点使各面机会均等。
 *
 * 生长判定与方块放置完全复用原版公开成员（BuddingAmethystBlock.canClusterGrowAtState、
 * AmethystClusterBlock.FACING/WATERLOGGED），不复制原版内部逻辑；
 * 与原版 randomTick 的唯一差别是去掉了 1/5 随机门槛并保证一次骨粉必有效果（无空转）。
 * 其他方块一律不干预，交还原版逻辑。
 */
@Mixin(BoneMealItem.class)
public abstract class AmethystBoneMealMixin {

    /** 紫水晶芽的下一生长阶段映射 */
    private static final Map<Block, Block> AMETHYST_NEXT_STAGE = Map.of(
        Blocks.SMALL_AMETHYST_BUD, Blocks.MEDIUM_AMETHYST_BUD,
        Blocks.MEDIUM_AMETHYST_BUD, Blocks.LARGE_AMETHYST_BUD,
        Blocks.LARGE_AMETHYST_BUD, Blocks.AMETHYST_CLUSTER
    );

    /** 母岩六个生长朝向（与原版 BuddingAmethystBlock.DIRECTIONS 同一集合） */
    private static final Direction[] GROW_DIRECTIONS = Direction.values();

    @Inject(
        method = "growCrop(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void vplGrowAmethyst(ItemStack boneMeal, Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!CarpetVPLAdditionSettings.amethystBoneMeal || !(level instanceof ServerLevel serverLevel)) {
            return; // 客户端不做预测，与服务端结果由原版同步机制对齐
        }
        BlockState state = serverLevel.getBlockState(pos);
        Block nextStage = AMETHYST_NEXT_STAGE.get(state.getBlock());

        if (nextStage != null) {
            // 芽本体被直接点到：推进一档，withPropertiesOf 保留 FACING/WATERLOGGED 朝向
            serverLevel.setBlock(pos, nextStage.withPropertiesOf(state), Block.UPDATE_ALL);
            consumeAndShow(boneMeal, serverLevel, pos);
            cir.setReturnValue(true);
            return;
        }

        if (!state.is(Blocks.BUDDING_AMETHYST)) {
            return; // 非紫水晶相关方块，交还原版逻辑
        }

        // 母岩：从随机起点依次尝试 6 个面，第一个能长的就长
        int start = serverLevel.getRandom().nextInt(GROW_DIRECTIONS.length);
        for (int i = 0; i < GROW_DIRECTIONS.length; i++) {
            Direction direction = GROW_DIRECTIONS[(start + i) % GROW_DIRECTIONS.length];
            BlockPos target = pos.relative(direction);
            BlockState targetState = serverLevel.getBlockState(target);
            Block growth = nextGrowthAt(targetState, direction);
            if (growth == null) {
                continue;
            }
            serverLevel.setBlockAndUpdate(target, growth.defaultBlockState()
                .setValue(AmethystClusterBlock.FACING, direction)
                .setValue(AmethystClusterBlock.WATERLOGGED, targetState.getFluidState().is(Fluids.WATER)));
            consumeAndShow(boneMeal, serverLevel, target);
            cir.setReturnValue(true);
            return;
        }
        // 六个面都长不了：不消耗骨粉，不返回 true（避免"白花一次"）
    }

    /** 该朝向的目标位置能长出什么：空/水出新芽，已有同朝向芽则推进一档，否则 null */
    private static Block nextGrowthAt(BlockState targetState, Direction direction) {
        if (BuddingAmethystBlock.canClusterGrowAtState(targetState)) {
            return Blocks.SMALL_AMETHYST_BUD;
        }
        Block advanced = AMETHYST_NEXT_STAGE.get(targetState.getBlock());
        if (advanced != null && targetState.getValue(AmethystClusterBlock.FACING) == direction) {
            return advanced;
        }
        return null;
    }

    /** 消耗一次骨粉并播放原版催熟粒子 */
    private static void consumeAndShow(ItemStack boneMeal, ServerLevel serverLevel, BlockPos pos) {
        boneMeal.shrink(1);
        BoneMealItem.addGrowthParticles(serverLevel, pos, 1);
    }
}
