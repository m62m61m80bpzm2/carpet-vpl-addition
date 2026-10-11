package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 粉色羊生成概率倍率（b1.17.1.1 新增，规则 pinkSheepSpawnMultiplier）。
 *
 * 原版机制（26.1~26.3 同构）：羊生成时 Sheep.finalizeSpawn 按所在地气候三档
 * （SheepColorSpawnRules：温带/温暖/寒冷）roll 颜色，"主色调"权重 82/100，
 * 主色调内部 499 份本体 + 1 份粉色 → 粉色羊概率恒定 = 0.82 × (1/500) = 0.164%（约 1/610）。
 *
 * 本 Mixin 在 finalizeSpawn 返回后按倍率精确换算（probability redistribution）：
 *  - 目标概率 target = 0.164% × multiplier
 *  - multiplier > 1：非粉羊以 (target − p0) / (1 − p0) 的概率翻转为粉色
 *  - multiplier < 1：粉羊以 multiplier 的概率保留，否则回退为白色
 * 数学上保证最终粉羊频率精确等于 原版概率 × 倍率。
 *
 * 影响所有新生成的羊（自然生成、繁殖、刷怪蛋等 finalizeSpawn 路径），
 * 存档中已存在的羊不变。26.1.2 / 26.3 的 finalizeSpawn 签名一致，一个 mixin 通吃。
 */
@Mixin(Sheep.class)
public abstract class SheepColorSpawnMultiplierMixin {

    /** 原版粉色羊生成概率：82/100（主色调权重）× 1/500（主色调内粉色份数） */
    private static final float VANILLA_PINK_CHANCE = 0.82f * (1.0f / 500.0f);
    /** 倍率不足时的回退颜色：三气候主色调之外的另一大宗色，最不突兀 */
    private static final DyeColor FALLBACK_COLOR = DyeColor.WHITE;

    @Inject(
        method = "finalizeSpawn",
        at = @At("TAIL")
    )
    private void vplPinkSheepChance(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason,
                                    SpawnGroupData groupData, CallbackInfoReturnable<SpawnGroupData> cir) {
        float multiplier = CarpetVPLAdditionSettings.pinkSheepSpawnMultiplierCached;
        if (multiplier == 1.0f || multiplier < 0.0f) {
            return;
        }
        Sheep self = (Sheep) (Object) this;
        if (!(level instanceof net.minecraft.server.level.ServerLevel) || !(self.getColor() instanceof DyeColor current)) {
            return;
        }
        float target = VANILLA_PINK_CHANCE * multiplier;
        RandomSource random = level.getRandom();

        if (current == DyeColor.PINK) {
            // 目标低于原版：粉羊按比例回退为白色
            if (multiplier < 1.0f && random.nextFloat() >= multiplier) {
                self.setColor(FALLBACK_COLOR);
            }
        } else if (multiplier > 1.0f) {
            // 目标高于原版：非粉羊按精确概率翻转为粉色
            float flipChance = (target - VANILLA_PINK_CHANCE) / (1.0f - VANILLA_PINK_CHANCE);
            if (random.nextFloat() < flipChance) {
                self.setColor(DyeColor.PINK);
            }
        }
    }
}
