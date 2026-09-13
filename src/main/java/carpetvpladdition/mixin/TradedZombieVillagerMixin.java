package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 和平模式保护交易过的僵尸村民（b1.15.1.1 新增，规则 tradedZombieVillagerPeace）。
 *
 * 原版 Mob.checkDespawn 在和平难度直接移除敌对生物，僵尸村民（含已被玩家
 * 交易过、治愈后带折扣的）也会被清空，交易收益全部丢失。本 Mixin 在
 * checkDespawn 的和平分支前拦截：交易过的僵尸村民（gossip 中存在 TRADING
 * 记录）在和平模式下不再被移除，玩家可以切回困难难度治愈它。
 */
@Mixin(Mob.class)
public abstract class TradedZombieVillagerMixin {

    @Inject(method = "checkDespawn", at = @At("HEAD"), cancellable = true)
    private void vplKeepTradedZombieVillager(CallbackInfo ci) {
        // 顺序优化（b1.15.1.2）：难度判断最便宜且绝大多数情况非和平，先短路避免对所有 Mob 做 instanceof
        if (!CarpetVPLAdditionSettings.tradedZombieVillagerPeace
            || ((Mob) (Object) this).level().getDifficulty() != Difficulty.PEACEFUL) {
            return;
        }
        if ((Object) this instanceof ZombieVillager zombieVillager && hasTradingGossip(zombieVillager)) {
            ci.cancel();
        }
    }

    /** 是否存在玩家交易记录（gossip 中的 TRADING 条目，由被交易过的村民转化时继承） */
    private static boolean hasTradingGossip(ZombieVillager zombieVillager) {
        var gossips = ((ZombieVillagerAccessor) zombieVillager).vpl$getGossips();
        return gossips != null && gossips.getCountForType(GossipType.TRADING, value -> true) > 0;
    }
}
