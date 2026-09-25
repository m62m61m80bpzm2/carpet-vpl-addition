package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 可挖掘紫水晶母岩（b1.15.1.1 新增，规则 minableBuddingAmethyst）。
 *
 * 注入 ServerPlayerGameMode.destroyBlock HEAD（服务端玩家破坏方块统一入口）：
 * 破坏的是紫水晶母岩、主手带精准采集、非创造模式时，在方块位置补发一个
 * 母岩掉落物（原版母岩 loot 无任何掉落，农场无法搬迁）。普通镐不干预。
 */
@Mixin(ServerPlayerGameMode.class)
public abstract class MinableBuddingAmethystMixin {

    @Shadow
    @Final
    protected ServerPlayer player;

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void vplMineBuddingAmethyst(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!CarpetVPLAdditionSettings.minableBuddingAmethyst) {
            return;
        }
        ServerPlayer serverPlayer = this.player;
        if (serverPlayer == null || serverPlayer.isCreative()
            || !serverPlayer.level().getBlockState(pos).is(Blocks.BUDDING_AMETHYST)) {
            return;
        }
        // 主手精准采集判定（26.2：经 registryAccess 查询附魔 Holder）
        ItemStack tool = serverPlayer.getMainHandItem();
        if (EnchantmentHelper.getItemEnchantmentLevel(
            serverPlayer.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH),
            tool) <= 0) {
            return;
        }
        // 补发母岩掉落物（破坏流程前生成；方块本体照常移除且无原版掉落，不会重复）
        Block.popResource(serverPlayer.level(), pos, new ItemStack(Blocks.BUDDING_AMETHYST));
    }
}
