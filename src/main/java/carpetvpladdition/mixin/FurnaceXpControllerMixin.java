package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class FurnaceXpControllerMixin {
    @ModifyVariable(
        method = "createExperience",
        at = @At("HEAD"),
        index = 3,
        argsOnly = true
    )
    private float multiplyXpPerItem(float xpPerItem) {
        try {
            float multiplier = Float.parseFloat(CarpetVPLAdditionSettings.furnaceXpMultiplier);
            if (multiplier != 1.0F) {
                return xpPerItem * multiplier;
            }
        } catch (NumberFormatException e) {
            System.err.println("[carpet-vpl-addition] Invalid furnaceXpMultiplier value: " + CarpetVPLAdditionSettings.furnaceXpMultiplier);
        }
        return xpPerItem;
    }
}
