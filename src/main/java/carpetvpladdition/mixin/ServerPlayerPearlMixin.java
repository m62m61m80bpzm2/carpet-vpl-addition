package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 珍珠区块加载修复（b1.14.4.2 新增，规则 pearlChunkKeepalive）。
 *
 * 修复的原版 bug（1.21.5+ 珍珠 ticket 机制）：
 *  - 玩家退出重进时，原版 loadAndSpawnEnderPearl 只为珍珠放置一次
 *    ENDER_PEARL ticket（半径 2 区块，超时仅 40gt），之后完全依赖
 *    "区块加载成功 → 珍珠 tick → 续期"这一链条；
 *  - chunk 系统调度偶发竞态会断掉链条：ticket 过期时区块还没加载完成
 *    → 区块卸载 → 珍珠永不 tick → 永不续期 → 永久死锁，
 *    珍珠悬空冻结、无法完成传送。
 *
 * 修复方式（最小干预）：在原版放置 ticket 之后，同步强制加载珍珠所在
 * 区块（getChunk FULL, true，仅一次）。区块确定就位后珍珠立即开始 tick，
 * 由原版机制自行续期接管，无需持续干预。开销为 1 个区块的加载，可忽略。
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerPearlMixin {

    @Inject(
        method = "loadAndSpawnEnderPearl",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;placeEnderPearlTicket(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/ChunkPos;)J",
            shift = At.Shift.AFTER
        )
    )
    private void ensurePearlChunkLoaded(ValueInput pearlInput, CallbackInfo ci,
                                        @Local(type = Entity.class) Entity pearl) {
        if (CarpetVPLAdditionSettings.pearlChunkKeepalive) {
            // 同步强制加载珍珠所在区块（FULL 状态），消除原版初始 ticket 的加载竞态
            // （26.2 的 ChunkPos.x/z 为 private，改由方块坐标右移 4 位求区块坐标）
            pearl.level().getChunk(pearl.blockPosition().getX() >> 4,
                pearl.blockPosition().getZ() >> 4, ChunkStatus.FULL, true);
        }
    }
}
