package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.network.protocol.game.ServerboundPickItemFromBlockPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 26.2 中 Ctrl+中键复制方块 NBT 失效的问题。
 * 原版客户端可能不再发送 includeData=true，导致创造模式下也无法复制方块 NBT（如箱子内容）。
 * 此 Mixin 将 includeData 强制设为 true，使创造模式玩家始终复制带 NBT 的方块。
 */
@Mixin(targets = "net/minecraft/server/network/ServerGamePacketListenerImpl")
public abstract class PickBlockNbtMixin {

    @Redirect(
        method = "handlePickItemFromBlock",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ServerboundPickItemFromBlockPacket;includeData()Z")
    )
    private boolean forceIncludeData(ServerboundPickItemFromBlockPacket packet) {
        if (CarpetVPLAdditionSettings.pickBlockNbt) {
            return true;
        }
        return packet.includeData();
    }
}
