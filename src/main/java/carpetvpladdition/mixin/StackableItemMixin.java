package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.SignItem;
import org.spongepowered.asm.mixin.Mixin;

/**
 * 修改物品最大堆叠数。
 *
 * 26.2 API变更：getMaxStackSize() 从 ItemStack 移动到 ItemInstance 接口的 default 方法
 * （读取 DataComponents.MAX_STACK_SIZE 组件），ItemStack 自身不再重写该方法。
 * 因此本 Mixin 实现 ItemInstance 接口并直接覆盖 getMaxStackSize()，从而替换 default 实现。
 * （1.14.2 曾改用 @Inject ItemStack.getMaxStackSize + require=0，在 26.2 上会静默失效，
 *  b1.14.3.1 已恢复 1.13.4 验证过的 implements 方案。）
 *
 * 性能优化（解决 MSPT 突然飙升的主因）：
 * 1. 如果所有堆叠规则都关闭，且漏斗矿车堆叠数为1，则直接返回原始值，不侵入热路径
 * 2. hopperMinecartStackSize 使用缓存 int 值，避免运行时反复 Integer.parseInt
 */
@Mixin(ItemStack.class)
public abstract class StackableItemMixin implements ItemInstance {

    @Override
    public int getMaxStackSize() {
        // 快速跳过：没有任何堆叠规则开启时直接返回原始值
        if (!anyStackableEnabled() && CarpetVPLAdditionSettings.hopperMinecartStackSizeCached <= 1) {
            return this.getOrDefault(DataComponents.MAX_STACK_SIZE, 1);
        }

        ItemStack self = (ItemStack) (Object) this;
        Item item = self.getItem();

        if (CarpetVPLAdditionSettings.stackableTotem && self.is(Items.TOTEM_OF_UNDYING)) {
            return 64;
        }

        if (CarpetVPLAdditionSettings.stackableLavaBucket && self.is(Items.LAVA_BUCKET)) {
            return 64;
        }

        if (CarpetVPLAdditionSettings.stackableBucket && self.is(Items.BUCKET)) {
            return 64;
        }

        if (CarpetVPLAdditionSettings.stackableGlassBottle && self.is(Items.GLASS_BOTTLE)) {
            return 64;
        }

        if (CarpetVPLAdditionSettings.stackableWaterBucket && self.is(Items.WATER_BUCKET)) {
            return 64;
        }

        if (CarpetVPLAdditionSettings.stackableMilkBucket && self.is(Items.MILK_BUCKET)) {
            return 64;
        }

        if (CarpetVPLAdditionSettings.stackablePowderSnowBucket && self.is(Items.POWDER_SNOW_BUCKET)) {
            return 64;
        }

        if (CarpetVPLAdditionSettings.stackableEnderPearl && self.is(Items.ENDER_PEARL)) {
            return 64;
        }

        if (CarpetVPLAdditionSettings.stackableMusicDisc && self.has(DataComponents.JUKEBOX_PLAYABLE)) {
            return 64;
        }

        if (CarpetVPLAdditionSettings.stackableSign && (item instanceof SignItem || item instanceof HangingSignItem)) {
            return 64;
        }

        if (CarpetVPLAdditionSettings.stackablePotion && item instanceof PotionItem) {
            return 64;
        }

        if (CarpetVPLAdditionSettings.stackableStew) {
            Item sItem = item;
            if (sItem == Items.MUSHROOM_STEW || sItem == Items.SUSPICIOUS_STEW ||
                sItem == Items.RABBIT_STEW || sItem == Items.BEETROOT_SOUP) {
                return 64;
            }
        }

        if (CarpetVPLAdditionSettings.stackableCake && self.is(Items.CAKE)) {
            return 64;
        }

        // 使用缓存的 int 值，避免 parseInt
        if (self.is(Items.HOPPER_MINECART) && CarpetVPLAdditionSettings.hopperMinecartStackSizeCached > 1) {
            return CarpetVPLAdditionSettings.hopperMinecartStackSizeCached;
        }

        return this.getOrDefault(DataComponents.MAX_STACK_SIZE, 1);
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
