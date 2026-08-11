package carpetvpladdition.util;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 信标统一 PP 更新管理器（b1.14.3.1 新增，b1.14.3.6 重构）。
 *
 * 功能：开启规则 beaconUnifiedPPUpdate 后，每隔 20 游戏刻（GT），向所有"达到要求"的信标
 * ——即正下方为【浅层青金石原矿】（minecraft:lapis_ore，石头变体，非深板岩变体）的信标——
 * 统一发出一次 PP 更新（方块更新中的 post-place / shape update）。
 *
 * 为什么是 PP 更新而不是 NC 更新：
 *  - 26.2 的侦测器（ObserverBlock）不重写 neighborChanged（NC 更新入口），只在
 *    updateShape（PP 更新入口）中响应：`if (FACING == directionToNeighbour && !POWERED) startSignal(...)`。
 *  - 因此必须对信标位置调用 BlockState.updateNeighbourShapes(...)（等价于"信标被重新放置"时
 *    向 6 个邻居发出的形状更新），信标周围的侦测器才会被触发。
 *
 * 统一性保证（TE 阶段最前面，微时序一致）：
 *  1. 由 BeaconTickPhaseMixin 注入 Level.tickBlockEntities 的 HEAD——即【方块实体（TE）阶段
 *     的最前面】、在遍历并 tick 任何方块实体之前执行；
 *  2. 该阶段内按 TreeSet（坐标排序）遍历本维度全部符合条件的信标并连续发出 PP 更新，
 *     100 个信标的更新顺序固定、连续执行，中间不会插入其他方块实体的 tick；
 *  3. 各维度在其各自的 TE 阶段最前面更新（信标本身是方块实体，其检测逻辑在该阶段内正常运行）。
 *
 * 追踪机制（始终追踪，规则仅控制是否触发，保证游戏中途开启规则也能生效）：
 *  - BeaconBlockEntityTrackerMixin 注入 BlockEntity.setLevel：信标被放置 / /setblock /
 *    区块加载时都会调用 setLevel，此时把位置加入追踪集合（已加载区块内放置也能捕获）；
 *  - ServerBlockEntityEvents.BLOCK_ENTITY_LOAD / UNLOAD：覆盖区块加载 / 卸载生命周期；
 *  - 触发时自愈：位置上方已不是信标（被破坏 / 被替换）则移出集合。
 */
public final class BeaconPPUpdateManager {

    /** 统一更新周期：20 游戏刻 = 1 秒 */
    private static final int PP_INTERVAL = 20;

    /** 维度 -> 已加载信标位置集合（TreeSet 保证每次处理顺序稳定） */
    private static final Map<ResourceKey<Level>, Set<BlockPos>> BEACONS = new HashMap<>();

    private static boolean registered = false;

    private BeaconPPUpdateManager() {
    }

    /** 注册追踪事件（幂等，只在 onGameStarted 调用一次）。不再注册 tick 钩子，由 BeaconTickPhaseMixin 驱动。 */
    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        // 区块加载 / 卸载生命周期：加载时加入追踪，卸载时移出
        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, world) -> {
            if (blockEntity instanceof BeaconBlockEntity && world instanceof ServerLevel serverLevel) {
                track(serverLevel, blockEntity.getBlockPos());
            }
        });
        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, world) -> {
            if (blockEntity instanceof BeaconBlockEntity && world instanceof ServerLevel serverLevel) {
                untrack(serverLevel, blockEntity.getBlockPos());
            }
        });

        // 世界卸载时清空对应维度的追踪集合
        ServerLevelEvents.UNLOAD.register((server, level) -> BEACONS.remove(level.dimension()));

        // 服务器停止时清空全部追踪
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> BEACONS.clear());
    }

    /** 将指定位置加入追踪（由 BeaconBlockEntityTrackerMixin 调用） */
    public static void track(ServerLevel level, BlockPos pos) {
        BEACONS.computeIfAbsent(level.dimension(), k -> new TreeSet<>()).add(pos.immutable());
    }

    /** 将指定位置移出追踪 */
    public static void untrack(ServerLevel level, BlockPos pos) {
        Set<BlockPos> set = BEACONS.get(level.dimension());
        if (set != null) {
            set.remove(pos);
        }
    }

    /**
     * 由 BeaconTickPhaseMixin 在【TE 阶段最前面】（Level.tickBlockEntities HEAD）调用：
     * 每隔 20GT，给当前维度所有符合条件的信标连续统一发出一次 PP 更新。
     * 所有信标在同一阶段内按坐标顺序连续处理，微时序一致，不打印任何输出。
     */
    public static void updateBeacons(ServerLevel level) {
        if (!CarpetVPLAdditionSettings.beaconUnifiedPPUpdate) {
            return;
        }
        if (level.getServer().getTickCount() % PP_INTERVAL != 0) {
            return;
        }

        Set<BlockPos> set = BEACONS.get(level.dimension());
        if (set == null || set.isEmpty()) {
            return;
        }
        Iterator<BlockPos> it = set.iterator();
        while (it.hasNext()) {
            BlockPos pos = it.next();
            // 自愈：该位置已不是信标（被破坏 / 被替换），移出追踪
            if (!level.getBlockState(pos).is(Blocks.BEACON)) {
                it.remove();
                continue;
            }
            // 达到要求：正下方是浅层青金石原矿（minecraft:lapis_ore，非深板岩变体）
            if (!level.getBlockState(pos.below()).is(Blocks.LAPIS_ORE)) {
                continue;
            }
            // PP 更新：等价于"信标被重新放置"时向 6 个邻居发出的形状更新。
            // 侦测器等方块通过 updateShape 响应（NC 更新它们不响应）。
            level.getBlockState(pos).updateNeighbourShapes(level, pos, Block.UPDATE_NEIGHBORS);
        }
    }
}
