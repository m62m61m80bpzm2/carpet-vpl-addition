package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 命名矿车区块加载器（b1.15.1.1 新增，规则 namedMinecartChunkLoader）。
 *
 * 在铁砧中把矿车物品命名为 "chunkloader"（不区分大小写），放置后该矿车
 * 即成为区块加载器：每 20 游戏刻为其所在区块续期一次加载票据
 * （复用原版末影珍珠 ticket：半径 2 区块、40gt 超时，public static 可直接调用），
 * 保证矿车持续行驶、沿途区块正常加载。
 */
@Mixin(AbstractMinecart.class)
public abstract class NamedMinecartChunkLoaderMixin {

    /** 区块加载器矿车的约定名称 */
    private static final String LOADER_NAME = "chunkloader";

    @Inject(method = "tick", at = @At("HEAD"))
    private void vplKeepChunkLoaded(CallbackInfo ci) {
        if (!CarpetVPLAdditionSettings.namedMinecartChunkLoader) {
            return;
        }
        AbstractMinecart cart = (AbstractMinecart) (Object) this;
        // 顺序优化（b1.15.1.2）：先按 20 刻节流（每矿车 19/20 的调用在此直接返回）
        if (cart.tickCount % 20 != 0
            || cart.level().isClientSide()
            || !(cart.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Component customName = cart.getCustomName();
        if (customName == null || !LOADER_NAME.equalsIgnoreCase(customName.getString())) {
            return;
        }
        // 复用原版末影珍珠票据逻辑（public static）：半径 2 区块，40gt 超时，每秒续期
        ServerPlayer.placeEnderPearlTicket(serverLevel, cart.chunkPosition());
    }
}
