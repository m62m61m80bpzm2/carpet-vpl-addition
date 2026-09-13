package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.SculkShriekerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 禁止生成监守者（b1.15.1.1 新增，规则 noWardenSpawn）。
 *
 * 注入幽匿尖啸体方块实体的 trySummonWarden（尖啸警告达到上限后尝试召唤
 * 监守者的唯一入口）：规则开启时直接取消（返回 false），尖啸体仍会发出
 * 尖啸警告音效，但不再召唤监守者。深暗之域可放心挖掘。
 */
@Mixin(SculkShriekerBlockEntity.class)
public abstract class NoWardenSpawnMixin {

    @Inject(method = "trySummonWarden", at = @At("HEAD"), cancellable = true)
    private void vplNoWarden(ServerLevel level, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetVPLAdditionSettings.noWardenSpawn) {
            cir.setReturnValue(false);
        }
    }
}
