package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import carpetvpladdition.util.EntityTypeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 禁止僵尸马自然生成（b1.14.3.2 新增，b1.14.3.8 修复）。
 *
 * 修复原因：旧实现注入 ZombieHorse.finalizeSpawn 并返回 null，
 * 但 NaturalSpawner 只是把返回值赋给 groupData、从不检查是否为 null，
 * 实体照样 addFreshEntityWithPassengers 生成——规则完全无效。
 *
 * 正确拦截点：SpawnPlacements 为僵尸马注册的生成前置判断
 * Monster.checkMonsterSpawnRules（ZombieHorse 注册的是 Monster::checkMonsterSpawnRules），
 * NaturalSpawner.isValidSpawnPostitionForType 在生成前调用
 * SpawnPlacements.checkSpawnRules(type, level, EntitySpawnReason.NATURAL, pos, random)，
 * 此时直接返回 false，僵尸马不会进入生成流程。
 *
 * 只拦 NATURAL（夜间刷怪）：刷怪蛋 / 指令 / 繁殖等其他生成路径不受影响。
 * 类型比较走 EntityTypeHelper（BuiltInRegistries），避免 26.1/26.2 常量位置差异。
 */
@Mixin(Monster.class)
public abstract class ZombieHorseSpawnMixin {

    private static final EntityType<?> ZOMBIE_HORSE_TYPE = EntityTypeHelper.get("zombie_horse");

    @Inject(method = "checkMonsterSpawnRules", at = @At("HEAD"), cancellable = true)
    private static void onCheckMonsterSpawnRules(EntityType<? extends Mob> type, ServerLevelAccessor level,
                                                 EntitySpawnReason spawnReason, BlockPos pos, RandomSource random,
                                                 CallbackInfoReturnable<Boolean> cir) {
        if (CarpetVPLAdditionSettings.noZombieHorseSpawn
            && spawnReason == EntitySpawnReason.NATURAL
            && type == ZOMBIE_HORSE_TYPE) {
            cir.setReturnValue(false);
        }
    }
}
