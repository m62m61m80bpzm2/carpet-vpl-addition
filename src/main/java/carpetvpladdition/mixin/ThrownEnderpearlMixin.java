package carpetvpladdition.mixin;

import carpetvpladdition.util.PearlChunkKeepaliveManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 珍珠追踪 Mixin（b1.14.4.2 新增，服务于 pearlChunkKeepalive 规则）。
 *
 * 注入 ThrownEnderpearl.setOwner(EntityReference)（Projectile 的 protected 重写）：
 *  - 投掷珍珠时：EnderpearlItem / Projectile 均会调用 setOwner；
 *  - 玩家退出重进时：Projectile.readAdditionalSaveData 会调用
 *    setOwner(EntityReference.read(input, "Owner")) 恢复 owner 引用——
 *    这正是"重进恢复的珍珠"（ticket 断档死锁场景）的必经之路。
 * 因此单注入点即可覆盖"新投掷"与"重进恢复"两条路径。
 *
 * 移除不在此处理：PearlChunkKeepaliveManager 每 20gt 自愈清理已移除的珍珠。
 */
@Mixin(ThrownEnderpearl.class)
public abstract class ThrownEnderpearlMixin {

    @Inject(method = "setOwner(Lnet/minecraft/world/entity/EntityReference;)V", at = @At("HEAD"))
    private void onSetOwner(EntityReference<Entity> owner, CallbackInfo ci) {
        PearlChunkKeepaliveManager.track((ThrownEnderpearl) (Object) this);
    }
}
