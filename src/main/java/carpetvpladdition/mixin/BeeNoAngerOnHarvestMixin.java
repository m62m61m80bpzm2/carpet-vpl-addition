package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.CampfireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 采蜜不惊蜂（b1.15.1.4 新增，规则 beeNoAngerOnHarvest）。
 *
 * 原版 BeehiveBlock.useItemOn 在剪下蜜脾或用玻璃瓶取蜜后，用
 * CampfireBlock.isSmokeyPos（蜂巢下方 1~5 格有无点燃篝火）决定走哪条分支：
 *  - 无熏烟 → angerNearbyBees 让巢外蜜蜂锁定玩家，并以 EMERGENCY 状态释放巢内蜂
 *    （BeehiveBlockEntity.emptyAllLivingFromHive 里对近距蜜蜂 bee.setTarget(player)）；
 *  - 有熏烟 → 仅 resetHoneyLevel，蜜蜂不出巢、不产生仇恨。
 *
 * 本 Mixin 只在该判定处重定向为"规则开启即视为已被熏"，等价于蜂巢底下永远压着篝火，
 * 一处注入同时覆盖巢外与巢内两条仇恨路径，不复制任何原版逻辑。
 *
 * 刻意不干预的相邻机制：玩家用工具砸毁蜂巢（playerDestroy）与爆炸破坏（onExplosionHit）
 * 不经过该判定，蜜蜂照旧仇恨——只免"取蜜"这一种情形。
 */
@Mixin(BeehiveBlock.class)
public abstract class BeeNoAngerOnHarvestMixin {

    @Redirect(
        method = "useItemOn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/CampfireBlock;isSmokeyPos("
                + "Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z"
        )
    )
    private boolean vplTreatHiveAsSedated(Level level, BlockPos pos) {
        return CarpetVPLAdditionSettings.beeNoAngerOnHarvest || CampfireBlock.isSmokeyPos(level, pos);
    }
}
