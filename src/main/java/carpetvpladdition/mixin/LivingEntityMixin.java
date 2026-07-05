package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 修改最大氧气值。
 *
 * 性能修复：使用缓存的 maxAirCached，避免每次 getMaxAirSupply 时 parseInt。
 * getMaxAirSupply 在水下实体频繁调用，属于热路径。
 */
@Mixin(Entity.class)
public abstract class LivingEntityMixin {
    @ModifyReturnValue(method = "getMaxAirSupply", at = @At("RETURN"))
    private int modifyMaxAirSupply(int original) {
        if (!((Object) this instanceof Player)) return original;
        return CarpetVPLAdditionSettings.maxAirCached;
    }
}
