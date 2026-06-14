package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.portal.PortalShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

        if (serverLevel.random.nextFloat() >= 0.1f) return;

        int cx = bottomLeft.getX() + width / 2;
        int cy = bottomLeft.getY();
        int cz = bottomLeft.getZ();
        int count = 1 + serverLevel.random.nextInt(3);

        for (int i = 0; i < count; i++) {
            ZombifiedPiglin pigman = EntityType.ZOMBIFIED_PIGLIN.create(serverLevel, EntitySpawnReason.TRIGGERED);
            if (pigman != null) {
                double px = cx + serverLevel.random.nextDouble() * 4 - 2;
                double pz = cz + serverLevel.random.nextDouble() * 4 - 2;
                pigman.setPos(px, cy + 1, pz);
                pigman.setPersistenceRequired();
                serverLevel.addFreshEntity(pigman);
            }
        }
    }
}
