package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ObserverBlock.class)
public class ObserverTickControlMixin {

    @ModifyArg(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;I)V"),
        index = 2
    )
    private int modifyTickDelay(int delay) {
        if (CarpetVPLAdditionSettings.observerTickDelay != null) {
            try {
                int customDelay = Integer.parseInt(CarpetVPLAdditionSettings.observerTickDelay);
                if (customDelay > 0) return customDelay;
            } catch (NumberFormatException ignored) {}
        }
        return delay;
    }

    @ModifyArg(
        method = "startSignal",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ScheduledTickAccess;scheduleTick(Lnet/minecraft/core/BlockPos;Ljava/lang/Object;I)V"),
        index = 2,
        require = 0
    )
    private int modifySignalDelay(int delay) {
        if (CarpetVPLAdditionSettings.observerTickDelay != null) {
            try {
                int customDelay = Integer.parseInt(CarpetVPLAdditionSettings.observerTickDelay);
                if (customDelay > 0) return customDelay;
            } catch (NumberFormatException ignored) {}
        }
        return delay;
    }
}
