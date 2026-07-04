package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 禁止村民被攻击后涨价（包括被打的村民和目击者村民）
 *
 * Villager.onReputationEventFrom() 中：
 *   VILLAGER_HURT  → 被打的村民添加 MINOR_NEGATIVE 流言
 *   VILLAGER_KILLED → 目击者村民添加 MAJOR_NEGATIVE 流言
 * 本 mixin 在规则 villagerNoPriceOnAttack 开启时跳过这两个事件。
 */
@Mixin(Villager.class)
public abstract class VillagerNoPriceOnAttackMixin {

    @Inject(
        method = "onReputationEventFrom",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onReputationEvent(ReputationEventType type, Entity entity, CallbackInfo ci) {
        if (!CarpetVPLAdditionSettings.villagerNoPriceOnAttack) return;
        // 跳过被打和目击击杀的声望变化
        if (type == ReputationEventType.VILLAGER_HURT || type == ReputationEventType.VILLAGER_KILLED) {
            ci.cancel();
        }
    }
}
