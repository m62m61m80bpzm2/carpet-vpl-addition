package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 禁止积雪（disableSnow 规则）。
 *
 * 只拦截 Biome.shouldSnow（决定积雪层是否生成），返回 false 时积雪不会堆积；
 * 不拦截 Biome.shouldFreeze，因此水面结冰（生成冰方块）保持原版行为，不受影响。
 */
@Mixin(net.minecraft.server.level.ServerLevel.class)
public class NoSnowAccumulationMixin {

    @Redirect(
        method = "tickPrecipitation",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;shouldSnow(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z")
    )
    private boolean onShouldSnow(Biome biome, LevelReader level, BlockPos pos) {
        if (CarpetVPLAdditionSettings.disableSnow) {
            return false;
        }
        return biome.shouldSnow(level, pos);
    }
}
