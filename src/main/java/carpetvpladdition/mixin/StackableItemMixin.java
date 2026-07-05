package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.SignItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修改物品最大堆叠数。
 *
 * 性能优化（解决 MSPT 突然飙升的主因）：
 * 1. 如果所有堆叠规则都关闭，且漏斗矿车堆叠数为1，则直接返回，不侵入热路径
 * 2. hopperMinecartStackSize 使用缓存 int 值，避免运行时反复 Integer.parseInt
 */
@Mixin(ItemStack.class)
public abstract class StackableItemMixin {
    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void modifyMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        // 快速跳过：没有任何堆叠规则开启时直接返回，不侵入最热路径
        if (!anyStackableEnabled() && CarpetVPLAdditionSettings.hopperMinecartStackSizeCached <= 1) {
            return;
        }

        ItemStack self = (ItemStack) (Object) this;
        Item item = self.getItem();

        if (CarpetVPLAdditionSettings.stackableTotem && self.is(Items.TOTEM_OF_UNDYING)) {
            cir.setReturnValue(64);
            return;
        }

        if (CarpetVPLAdditionSettings.stackableLavaBucket && self.is(Items.LAVA_BUCKET)) {
            cir.setReturnValue(64);
            return;
        }

        if (CarpetVPLAdditionSettings.stackableBucket && self.is(Items.BUCKET)) {
            cir.setReturnValue(64);
            return;
        }

        if (CarpetVPLAdditionSettings.stackableGlassBottle && self.is(Items.GLASS_BOTTLE)) {
            cir.setReturnValue(64);
            return;
        }

        if (CarpetVPLAdditionSettings.stackableWaterBucket && self.is(Items.WATER_BUCKET)) {
            cir.setReturnValue(64);
            return;
        }

        if (CarpetVPLAdditionSettings.stackableMilkBucket && self.is(Items.MILK_BUCKET)) {
            cir.setReturnValue(64);
            return;
        }

        if (CarpetVPLAdditionSettings.stackablePowderSnowBucket && self.is(Items.POWDER_SNOW_BUCKET)) {
            cir.setReturnValue(64);
            return;
        }

        if (CarpetVPLAdditionSettings.stackableEnderPearl && self.is(Items.ENDER_PEARL)) {
            cir.setReturnValue(64);
            return;
        }

        if (CarpetVPLAdditionSettings.stackableMusicDisc && self.has(DataComponents.JUKEBOX_PLAYABLE)) {
            cir.setReturnValue(64);
            return;
        }

        if (CarpetVPLAdditionSettings.stackableSign && (item instanceof SignItem || item instanceof HangingSignItem)) {
            cir.setReturnValue(64);
            return;
        }

        if (CarpetVPLAdditionSettings.stackablePotion && item instanceof PotionItem) {
            cir.setReturnValue(64);
            return;
        }

        if (CarpetVPLAdditionSettings.stackableStew) {
            Item sItem = item;
            if (sItem == Items.MUSHROOM_STEW || sItem == Items.SUSPICIOUS_STEW ||
                sItem == Items.RABBIT_STEW || sItem == Items.BEETROOT_SOUP) {
                cir.setReturnValue(64);
                return;
            }
        }

        if (CarpetVPLAdditionSettings.stackableCake && self.is(Items.CAKE)) {
            cir.setReturnValue(64);
            return;
        }

        // 使用缓存的 int 值，避免 parseInt
        if (self.is(Items.HOPPER_MINECART) && CarpetVPLAdditionSettings.hopperMinecartStackSizeCached > 1) {
            cir.setReturnValue(CarpetVPLAdditionSettings.hopperMinecartStackSizeCached);
        }
    }

    private static boolean anyStackableEnabled() {
        return CarpetVPLAdditionSettings.stackableTotem
            || CarpetVPLAdditionSettings.stackableLavaBucket
            || CarpetVPLAdditionSettings.stackableBucket
            || CarpetVPLAdditionSettings.stackableGlassBottle
            || CarpetVPLAdditionSettings.stackableWaterBucket
            || CarpetVPLAdditionSettings.stackableMilkBucket
            || CarpetVPLAdditionSettings.stackablePowderSnowBucket
            || CarpetVPLAdditionSettings.stackableEnderPearl
            || CarpetVPLAdditionSettings.stackableMusicDisc
            || CarpetVPLAdditionSettings.stackableSign
            || CarpetVPLAdditionSettings.stackablePotion
            || CarpetVPLAdditionSettings.stackableStew
            || CarpetVPLAdditionSettings.stackableCake;
    }
}
