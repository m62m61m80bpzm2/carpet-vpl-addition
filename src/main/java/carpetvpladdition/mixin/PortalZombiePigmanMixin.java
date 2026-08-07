package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.portal.PortalShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 基岩版地狱门刷僵尸猪人。
 *
 * 修复：去掉 setPersistenceRequired()，避免实体不消失累积泄漏。
 * 僵尸猪人应像自然生成的怪物一样可以被正常清除。
 */
@Mixin(PortalShape.class)
public abstract class PortalZombiePigmanMixin {
    @Shadow @Final private Direction.Axis axis;
    @Shadow @Final private BlockPos bottomLeft;
    @Shadow @Final private int height;
    @Shadow @Final private int width;

    @Inject(method = "createPortalBlocks", at = @At("TAIL"))
    private void onCreatePortalBlocks(LevelAccessor level, CallbackInfo ci) {
        if (!CarpetVPLAdditionSettings.bedrockPortalZombiePigman) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (serverLevel.getRandom().nextFloat() >= 0.1f) return;

        int cx = bottomLeft.getX() + width / 2;
        int cy = bottomLeft.getY();
        int cz = bottomLeft.getZ();
        int count = 1 + serverLevel.getRandom().nextInt(3);

        for (int i = 0; i < count; i++) {
            ZombifiedPiglin pigman = EntityTypes.ZOMBIFIED_PIGLIN.create(serverLevel, EntitySpawnReason.TRIGGERED);
            if (pigman != null) {
                double px = cx + serverLevel.getRandom().nextDouble() * 4 - 2;
                double pz = cz + serverLevel.getRandom().nextDouble() * 4 - 2;
                pigman.setPos(px, cy + 1, pz);
                // 不设 setPersistenceRequired()，允许正常消失，避免实体泄漏
                serverLevel.addFreshEntity(pigman);
            }
        }
    }
}
