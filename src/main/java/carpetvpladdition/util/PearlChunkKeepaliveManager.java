package carpetvpladdition.util;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 珍珠区块保活管理器（b1.14.4.2 新增，规则 pearlChunkKeepalive）。
 *
 * 修复的原版缺陷（1.21.5+ 珍珠 ticket 机制的续期断档死锁）：
 *  - 原版：玩家退出重进时 loadAndSpawnEnderPearl 为珍珠加一次 ENDER_PEARL ticket
 *    （半径 2 区块，超时仅 40gt）；之后完全依赖"区块加载成功 → 珍珠 tick → 续期"。
 *    chunk 系统调度偶发竞态会导致初始 ticket 未能拉起区块 / 区块加载完成前 ticket
 *    已过期 → 区块卸载 → 珍珠不再 tick → 不再续期 → 永久死锁（珍珠冻结，
 *    珍珠传送站失效，需玩家手动跑过去加载一次才能恢复）。
 *  - 本管理器：每 20 游戏刻为所有存活珍珠所在区块重新放置 ticket（半径 2，
 *    与原版一致），即使原版续期断档也会被拉回，珍珠 tick 恢复后自动接管。
 *
 * 追踪机制：
 *  - ThrownEnderpearlMixin 注入 setOwner（Projectile.readAdditionalSaveData 加载路径
 *    与投掷路径都会调用）——同时覆盖"重进恢复的珍珠"与"新投掷的珍珠"；
 *  - 每 20gt 自愈：已 discard / 已移除的珍珠移出追踪集合（最多滞后一个周期，无泄漏）；
 *  - 珍珠命中即传送或 discard，追踪集合规模自然收敛，常驻开销可忽略。
 */
public final class PearlChunkKeepaliveManager {

    /** 续期周期：20 游戏刻 = 1 秒（原版 ticket 超时 40gt，周期取其一半保证无缝） */
    private static final int KEEPALIVE_INTERVAL = 20;

    /** 全部存活珍珠追踪集合（线程安全；tick 事件在主线程，setOwner 可能来自实体加载） */
    private static final Set<ThrownEnderpearl> PEARLS = ConcurrentHashMap.newKeySet();

    private static boolean registered = false;

    private PearlChunkKeepaliveManager() {
    }

    /** 注册 tick 钩子（幂等，只在 onGameStarted 调用一次）。 */
    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        ServerTickEvents.END_SERVER_TICK.register(PearlChunkKeepaliveManager::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> PEARLS.clear());
    }

    /** 将珍珠加入追踪（由 ThrownEnderpearlMixin 调用）。 */
    public static void track(ThrownEnderpearl pearl) {
        if (CarpetVPLAdditionSettings.pearlChunkKeepalive && pearl.level() instanceof ServerLevel) {
            PEARLS.add(pearl);
        }
    }

    /**
     * 每 20gt 为所有存活珍珠所在区块重新放置 ENDER_PEARL ticket（半径 2，与原版一致）。
     * 自愈：已移除的珍珠移出追踪集合。
     */
    private static void tick(MinecraftServer server) {
        if (!CarpetVPLAdditionSettings.pearlChunkKeepalive) {
            return;
        }
        if (server.getTickCount() % KEEPALIVE_INTERVAL != 0) {
            return;
        }

        Iterator<ThrownEnderpearl> it = PEARLS.iterator();
        while (it.hasNext()) {
            ThrownEnderpearl pearl = it.next();
            // 自愈：珍珠已 discard / 已移除（命中传送、落地消失等），移出追踪
            if (pearl.isRemoved() || !(pearl.level() instanceof ServerLevel level)) {
                it.remove();
                continue;
            }
            // 复用原版静态方法：给珍珠所在区块加 radius=2 的 ENDER_PEARL ticket
            ServerPlayer.placeEnderPearlTicket(level, pearl.chunkPosition());
        }
    }
}
