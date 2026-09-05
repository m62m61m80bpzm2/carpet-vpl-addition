package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 铁砧不损坏（b1.14.4.1 新增，规则 anvilNoDamage）。
 *
 * 原版每次使用铁砧后有 12% 概率调用 AnvilBlock.damage(state) 降级：
 *   完整铁砧 → 缺口铁砧 → 损坏铁砧 → null（消失）。
 * 规则开启时直接返回原 BlockState（不降级也不消失），
 * 覆盖 FallingBlockEntity 落地损坏以外的全部使用损耗路径。
 */
@Mixin(AnvilBlock.class)
public abstract class AnvilNoDamageMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private static void keepAnvilIntact(BlockState blockState, CallbackInfoReturnable<BlockState> cir) {
        if (CarpetVPLAdditionSettings.anvilNoDamage) {
            cir.setReturnValue(blockState);
        }
    }
}
