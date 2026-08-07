package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 禁止僵尸马自然生成（b1.14.3.2 新增，服务于 noZombieHorseSpawn 规则）。
 *
 * 26.2 中僵尸马通过 SpawnPlacements 注册的 Monster::checkMonsterSpawnRules 夜间自然生成。
 * 在 ZombieHorse.finalizeSpawn 的 HEAD 拦截：当生成原因为 NATURAL（夜间刷怪）且规则开启时，
 * 直接返回 null，生成方会丢弃该实体，从而阻止自然生成。
 *
 * 只拦截 NATURAL，刷怪蛋 / 指令 / 繁殖等其他生成路径不受影响（玩家主动行为不干预）。
 */
@Mixin(ZombieHorse.class)
public abstract class ZombieHorseSpawnMixin {

    @Inject(method = "finalizeSpawn", at = @At("HEAD"), cancellable = true)
    private void onFinalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                 EntitySpawnReason spawnReason, SpawnGroupData groupData,
                                 CallbackInfoReturnable<SpawnGroupData> cir) {
        if (CarpetVPLAdditionSettings.noZombieHorseSpawn && spawnReason == EntitySpawnReason.NATURAL) {
            cir.setReturnValue(null);
        }
    }
}
