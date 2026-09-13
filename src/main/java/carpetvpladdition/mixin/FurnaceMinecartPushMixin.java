package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 动力矿车编组不甩车（b1.15.1.1 新增，规则 furnaceMinecartNoSlip）。
 *
 * 原版动力矿车牵引编组过弯时，push 会把自身速度直接传给后车，弯道处产生
 * 侧向速度分量导致后车脱轨（"甩出"）。本 Mixin 重定向 pushEntities 中的
 * entity.push(pusher)：动力矿车推动其他矿车时，把后车速度钳制到动力矿车
 * 的运动切线方向（清除侧向分量、保留沿切线的前向分量），后车沿弯道行驶不再脱轨。
 * 非动力矿车互推、玩家乘坐等场景不受影响。
 */
@Mixin(NewMinecartBehavior.class)
public abstract class FurnaceMinecartPushMixin {

    @Redirect(
        method = "pushEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;push(Lnet/minecraft/world/entity/Entity;)V"
        )
    )
    private void vplGentlePush(Entity pushed, Entity pusher) {
        pushed.push(pusher);
        // 仅在规则开启且推动者是动力矿车、被推者是矿车时做钳制
        if (!CarpetVPLAdditionSettings.furnaceMinecartNoSlip
            || !(pusher instanceof MinecartFurnace furnace)
            || !(pushed instanceof AbstractMinecart cart)) {
            return;
        }
        Vec3 furnaceMovement = furnace.getDeltaMovement();
        if (furnaceMovement.horizontalDistanceSqr() <= 1.0E-6) {
            return;
        }
        // 动力矿车自身未脱轨，其水平速度方向即弯道切线方向
        Vec3 tangent = new Vec3(furnaceMovement.x, 0.0, furnaceMovement.z).normalize();
        Vec3 cartMovement = cart.getDeltaMovement();
        double along = cartMovement.dot(tangent);
        if (along > 0.0) {
            // 保留沿切线的前向分量，清除侧向分量 → 沿弯道行驶不甩出
            cart.setDeltaMovement(tangent.scale(along));
        } else {
            // 后车速度与切线反向（异常情况）：清掉水平速度仅保留垂直
            cart.setDeltaMovement(new Vec3(0.0, cartMovement.y, 0.0));
        }
    }
}
