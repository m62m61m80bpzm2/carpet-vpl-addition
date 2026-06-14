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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerAttractionMixin {
    @Inject(method = "customServerAiStep", at = @At("TAIL"))
    private void onCustomServerAiStep(ServerLevel level, CallbackInfo ci) {
        if (!CarpetVPLAdditionSettings.villagerAttraction) return;

        Villager self = (Villager) (Object) this;
        Player nearestPlayer = level.getNearestPlayer(self, 10.0);
        if (nearestPlayer == null || nearestPlayer.isSpectator()) return;

        ItemStack mainHand = nearestPlayer.getMainHandItem();
        ItemStack offHand = nearestPlayer.getOffhandItem();
        if (isTempting(self, mainHand) || isTempting(self, offHand)) {
            if (self.distanceToSqr(nearestPlayer) > 2.25) {
                self.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(nearestPlayer, 0.5F, 0));
            }
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
