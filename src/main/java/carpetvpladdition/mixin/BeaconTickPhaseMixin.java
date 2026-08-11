package carpetvpladdition.mixin;

import carpetvpladdition.util.BeaconPPUpdateManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * TE 阶段信标统一 PP 更新钩子（b1.14.3.6 新增）。
 *
 * 注入 Level.tickBlockEntities 的 HEAD——即【方块实体（TE）阶段的最前面】，
 * 在服务器开始遍历并 tick 任何方块实体之前执行。
 *
 * 为什么选这里：
 *  - 信标本身是方块实体（BE），其"信标线程"（BeaconBlockEntity.tick）在该阶段内
 *    逐 tick 检测下方方块；把统一 PP 更新放在 TE 阶段最前面，既保证所有信标的更新
 *    连续执行（同一循环内按坐标顺序处理，中间不插入其他 BE 的 tick），又与信标自身
 *    的检测逻辑处于同一阶段，时序最接近"信标线程"语义；
 *  - 每个维度独立调用自己的 tickBlockEntities，因此各维度信标在该维度 TE 阶段最前面更新。
 *
 * 仅对 ServerLevel 生效（客户端 Level 走不同分支，instanceof 过滤）。
 */
@Mixin(Level.class)
public abstract class BeaconTickPhaseMixin {

    @Inject(method = "tickBlockEntities", at = @At("HEAD"))
    private void onTickBlockEntitiesStart(CallbackInfo ci) {
        if ((Object) this instanceof ServerLevel serverLevel) {
            BeaconPPUpdateManager.updateBeacons(serverLevel);
        }
    }
}
