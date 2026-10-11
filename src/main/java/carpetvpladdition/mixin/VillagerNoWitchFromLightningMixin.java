package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.monster.Witch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 雷电不把村民劈成女巫（b1.17.1.1 新增，规则 villagerNoWitchFromLightning）。
 *
 * 原版机制（26.1~26.3 同构）：Villager.thunderHit 非和平难度时调用
 * Mob.convertTo(WITCH, ...) 把村民转换为女巫；转换失败（返回 null）时
 * 原版自身会走 fallback：super.thunderHit（普通雷击，掉血着火不变身）。
 *
 * 本 Mixin 将 convertTo 调用强制短路为 null，使原版 fallback 分支自然接管：
 * 村民照常受到雷击伤害但不会变成女巫（方案 A：只阻止变身，不提供免伤）。
 * 26.1.2 / 26.3 的 thunderHit 与 convertTo(EntityType, ConversionParams, AfterConversion)
 * 签名一致，一个 mixin 通吃。
 */
@Mixin(Villager.class)
public abstract class VillagerNoWitchFromLightningMixin {

    // 注意：1.21.8 的 thunderHit 里 convertTo 调用的字节码 owner 是 Villager 自身
    //（新版 javac 对 this. 泛型方法调用生成 owner=当前类），而 26.x 是 Mob——
    // 两分支代码各自独立，此处 owner 必须写 Villager，否则 0 target 开服崩。
    @Redirect(
        method = "thunderHit",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/npc/Villager;convertTo(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ConversionParams;Lnet/minecraft/world/entity/ConversionParams$AfterConversion;)Lnet/minecraft/world/entity/Mob;"
        )
    )
    private <T extends Mob> T vplNoWitchConversion(Villager instance, EntityType<T> entityType,
                                                   net.minecraft.world.entity.ConversionParams params,
                                                   net.minecraft.world.entity.ConversionParams.AfterConversion<T> afterConversion) {
        if (CarpetVPLAdditionSettings.villagerNoWitchFromLightning) {
            // 强制转换失败，原版 fallback 走 super.thunderHit（普通雷击，不变身）
            return null;
        }
        return instance.convertTo(entityType, params, afterConversion);
    }
}
