package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class StackableItemMixin {
    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void modifyMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        ItemStack self = (ItemStack) (Object) this;

        if (CarpetVPLAdditionSettings.stackableTotem && self.is(Items.TOTEM_OF_UNDYING)) {
            cir.setReturnValue(64);
        }

        if (CarpetVPLAdditionSettings.stackableLavaBucket && self.is(Items.LAVA_BUCKET)) {
            cir.setReturnValue(64);
        }

        if (CarpetVPLAdditionSettings.stackableBucket && self.is(Items.BUCKET)) {
            cir.setReturnValue(64);
        }

        if (CarpetVPLAdditionSettings.stackableGlassBottle && self.is(Items.GLASS_BOTTLE)) {
            cir.setReturnValue(64);
        }

        if (CarpetVPLAdditionSettings.stackableWaterBucket && self.is(Items.WATER_BUCKET)) {
            cir.setReturnValue(64);
        }
    }
}
