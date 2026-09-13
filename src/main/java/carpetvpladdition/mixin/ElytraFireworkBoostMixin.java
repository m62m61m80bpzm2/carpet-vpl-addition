package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 鞘翅烟花速度倍率（b1.15.1.1 新增，规则 elytraFireworkMultiplier）。
 *
 * 原版鞘翅推进加速度公式：movement.add(look*0.1 + (look*1.5 - movement)*0.5)，
 * 其中 1.5 为推进功率。本 Mixin 重定向该 movement.add(DDD) 调用（tick 中第 1 处，
 * 仅鞘翅飞行分支内），把加速度增量按规则倍率缩放：1.0 = 原版速度，
 * 2.0 = 推进加速度翻倍。其余烟花的正常飞行路径不受影响（不走该分支）。
 */
@Mixin(FireworkRocketEntity.class)
public abstract class ElytraFireworkBoostMixin {

    @Redirect(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;",
            ordinal = 0
        )
    )
    private Vec3 vplBoostFirework(Vec3 movement, double x, double y, double z) {
        double multiplier = CarpetVPLAdditionSettings.elytraFireworkMultiplierCached;
        if (multiplier == 1.0) {
            return movement.add(x, y, z);
        }
        return movement.add(x * multiplier, y * multiplier, z * multiplier);
    }
}
