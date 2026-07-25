package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 村民吸引跟随。
 *
 * 性能修复：
 * 1. 从每 tick 降频到每 10 tick (0.5秒) 扫描一次玩家
 * 2. 只有当玩家移动距离 > 0.75 格才更新寻路目标，
 *    避免每 tick 设新 WalkTarget 导致大脑反复取消 + 重新寻路
 *    （大型村民交易所中，上百村民同时反复寻路 = MSPT 几十甚至上百）
 */
@Mixin(Villager.class)
public abstract class VillagerAttractionMixin {
    @Unique
    private int scanCooldown = 0;

    @Unique
    private Player lastTargetPlayer = null;

    @Unique
    private double lastTargetX, lastTargetZ;

    @Inject(method = "customServerAiStep", at = @At("TAIL"))
    private void onCustomServerAiStep(ServerLevel level, CallbackInfo ci) {
        if (!CarpetVPLAdditionSettings.villagerAttraction) return;

        // 降频到每 10 tick 扫描一次
        if (--scanCooldown > 0) return;
        scanCooldown = 10;

        Villager self = (Villager) (Object) this;
        Player nearestPlayer = level.getNearestPlayer(self, 10.0);
        if (nearestPlayer == null || nearestPlayer.isSpectator()) return;

        ItemStack mainHand = nearestPlayer.getMainHandItem();
        ItemStack offHand = nearestPlayer.getOffhandItem();
        if (isTempting(self, mainHand) || isTempting(self, offHand)) {
            if (self.distanceToSqr(nearestPlayer) > 2.25) {
                // 仅在玩家移动超过 0.75 格时更新目标，避免反复触发寻路
                double dx = nearestPlayer.getX() - lastTargetX;
                double dz = nearestPlayer.getZ() - lastTargetZ;
                if (lastTargetPlayer != nearestPlayer || (dx * dx + dz * dz) > 0.5625) {
                    self.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(nearestPlayer, 0.5F, 0));
                    lastTargetPlayer = nearestPlayer;
                    lastTargetX = nearestPlayer.getX();
                    lastTargetZ = nearestPlayer.getZ();
                }
            }
        } else {
            lastTargetPlayer = null;
        }
    }

    private static boolean isTempting(Villager villager, ItemStack stack) {
        if (stack.is(Items.EMERALD_BLOCK)) return true;
        if (stack.is(Items.EMERALD) && villager.getVillagerData().profession().is(VillagerProfession.NONE)) return true;
        if (stack.is(Items.LAPIS_LAZULI) && !villager.getVillagerData().profession().is(VillagerProfession.NONE)) return true;
        if (stack.is(Items.CAKE) && villager.isBaby()) return true;
        return false;
    }
}
