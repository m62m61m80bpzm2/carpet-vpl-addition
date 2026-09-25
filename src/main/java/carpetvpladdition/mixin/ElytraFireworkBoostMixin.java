package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * 鞘翅烟花速度倍率（b1.15.1.1 新增，b1.15.1.6 修正缩放对象，规则 elytraFireworkMultiplier）。
 *
 * 原版 FireworkRocketEntity.tick 挂在玩家身上时的推进公式（26.1.2 / 26.2 源码一致）：
 *   movement.add(look*0.1 + (look*1.5 - movement)*0.5)
 * 其中 1.5 是"推进目标速度"，0.5 是朝该目标的平滑逼近系数，0.1 是一次性冲量。
 *
 * b1.15.1.1 曾把整个 add 的增量乘以倍率，等于连平滑逼近系数 0.5 也一起放大：
 * 速度在一刻内被硬顶到上限、下一刻又被滑行阻力拉回，表现为"一抽一抽"，
 * 而且极限速度并没有提高。现改为只缩放目标速度 1.5（tick 内三处 x/y/z 分量，
 * 另有一处赋给未被使用的局部变量 power，改了也无副作用），
 * 逼近节奏仍由原版 0.5 控制 → 加速平滑，稳定速度按倍率提升。
 */
@Mixin(FireworkRocketEntity.class)
public abstract class ElytraFireworkBoostMixin {

    @ModifyConstant(
        method = "tick",
        constant = @Constant(doubleValue = 1.5)
    )
    private double vplScaleBoostPower(double power) {
        float multiplier = CarpetVPLAdditionSettings.elytraFireworkMultiplierCached;
        return multiplier == 1.0f ? power : power * multiplier;
    }
}
