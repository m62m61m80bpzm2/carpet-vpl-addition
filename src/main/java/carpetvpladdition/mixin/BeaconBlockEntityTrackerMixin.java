package carpetvpladdition.mixin;

import carpetvpladdition.util.BeaconPPUpdateManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 信标追踪 Mixin（b1.14.3.1 新增，服务于 beaconUnifiedPPUpdate 规则）。
 *
 * 选择注入 BlockEntity.setLevel 的原因：
 *  - 玩家放置信标、/setblock、区块加载（含已加载区块内放置）都会触发 setLevel；
 *  - 而不注入 BeaconBlock.newBlockEntity，是因为该方法返回时方块实体的 level 尚未赋值，
 *    无法判断是否服务端维度、也无法拿到 ServerLevel 用于分组追踪。
 *
 * 注入时机为 RETURN（setLevel 执行完成），此时 this 的 level 已可用。
 * 仅对信标（BeaconBlockEntity）生效，其余方块实体只多一次 instanceof 判断，开销可忽略。
 */
@Mixin(BlockEntity.class)
public abstract class BeaconBlockEntityTrackerMixin {

    @Inject(method = "setLevel", at = @At("RETURN"))
    private void onSetLevel(Level level, CallbackInfo ci) {
        if ((Object) this instanceof BeaconBlockEntity && level instanceof ServerLevel serverLevel) {
            BeaconPPUpdateManager.track(serverLevel, ((BeaconBlockEntity) (Object) this).getBlockPos());
        }
    }
}
