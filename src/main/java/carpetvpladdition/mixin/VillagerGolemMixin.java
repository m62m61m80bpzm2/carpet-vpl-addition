package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CarvedPumpkinBlock.class)
public abstract class VillagerGolemMixin {
    @Inject(method = "trySpawnGolem", at = @At("TAIL"))
    private void onTrySpawnVillagerGolem(Level level, BlockPos pos, CallbackInfo ci) {
        if (!CarpetVPLAdditionSettings.villagerGolem) return;

        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        if (!belowState.is(Blocks.EMERALD_BLOCK)) return;

        level.setBlock(belowPos, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

        Villager villager = EntityType.VILLAGER.create(level, EntitySpawnReason.STRUCTURE);
        if (villager != null) {
            villager.setPos(belowPos.getX() + 0.5, belowPos.getY() + 0.05, belowPos.getZ() + 0.5);
            level.addFreshEntity(villager);
            level.playSound(null, belowPos, SoundEvents.VILLAGER_CELEBRATE, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
    }
}
