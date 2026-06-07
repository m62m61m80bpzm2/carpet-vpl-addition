package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerAttractionMixin {
    @Shadow
    protected GoalSelector goalSelector;

    @Inject(method = "registerBrainGoals", at = @At("TAIL"))
    private void onRegisterBrainGoals(CallbackInfo ci) {
        if (!CarpetVPLAdditionSettings.villagerAttraction) return;
        Villager self = (Villager) (Object) this;
        goalSelector.addGoal(1, new TemptGoal(self, 0.5, this::isTemptingItem, false));
    }

    private boolean isTemptingItem(ItemStack stack) {
        Villager self = (Villager) (Object) this;
        if (stack.is(Items.EMERALD_BLOCK)) return true;
        if (stack.is(Items.EMERALD) && self.getVillagerData().profession().is(VillagerProfession.NONE)) return true;
        if (stack.is(Items.LAPIS_LAZULI) && !self.getVillagerData().profession().is(VillagerProfession.NONE)) return true;
        if (stack.is(Items.CAKE) && self.isBaby()) return true;
        return false;
    }
}
