package carpetvpladdition.util;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 信标统一 PP 更新管理器（b1.14.3.1 新增）。
 *
 * 功能：开启规则 beaconUnifiedPPUpdate 后，每隔 20 游戏刻（GT），向所有"达到要求"的信标
 * ——即正下方为【浅层青金石原矿】（minecraft:lapis_ore，石头变体，非深板岩变体）的信标——
 * 统一发出一次方块更新。
 *
 * 统一性保证：
 *  1. 所有维度、所有位置的信标都在【服务器 tick 末尾阶段】（ServerTickEvents.END_SERVER_TICK，
 *     所有世界 tick 完成、计划刻与区块事件处理完毕之后）被更新，发出更新的时刻完全一致；
 *  2. 追踪集合使用 TreeSet（按坐标排序），每次触发的处理顺序固定，微时序保持一致；
 *  3. 每次触发在服务器控制台输出触发阶段、tick 号与更新数量，便于验证时序。
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

    /** 注册追踪事件与 tick 钩子（幂等，只在 onGameStarted 调用一次） */
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

        // 每隔 20GT 的统一更新钩子（阶段：服务器 tick 末尾）
        ServerTickEvents.END_SERVER_TICK.register(BeaconPPUpdateManager::onServerTick);
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
     * 服务器 tick 末尾阶段：每隔 20GT 给所有符合条件的信标统一发出一次方块更新。
     * 更新方式：对信标正下方的青金石原矿调用 updateNeighborsAt（等价于"重新放置青金石原矿"），
     * 信标作为其上方邻居收到方块更新，与其相邻的红石元件也会重新计算，产生可观测的统一脉冲。
     */
    private static void onServerTick(MinecraftServer server) {
        if (!CarpetVPLAdditionSettings.beaconUnifiedPPUpdate) {
            return;
        }
        if (server.getTickCount() % PP_INTERVAL != 0) {
            return;
        }

        int updated = 0;
        for (ServerLevel level : server.getAllLevels()) {
            Set<BlockPos> set = BEACONS.get(level.dimension());
            if (set == null || set.isEmpty()) {
                continue;
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
                // 向青金石原矿的 6 个邻居（含其上方的信标）发出方块更新
                level.updateNeighborsAt(pos.below(), Blocks.LAPIS_ORE);
                updated++;
            }
        }

        // 告知触发阶段：服务器 tick 末尾（END_SERVER_TICK），所有维度 tick 已完成。
        // 仅在确有更新时输出，避免每秒刷屏（无符合要求的信标时静默）。
        if (updated > 0) {
            System.out.println("[carpet-vpl-addition] beaconUnifiedPPUpdate: 阶段=服务器tick末尾(END_SERVER_TICK), tick="
                    + server.getTickCount() + ", 统一更新信标数=" + updated);
        }
    }
}
