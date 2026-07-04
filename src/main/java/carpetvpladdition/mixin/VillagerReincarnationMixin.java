package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import carpetvpladdition.util.CompoundTagValueOutput;
import carpetvpladdition.util.SpawnEggHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerReincarnationMixin {
    @Inject(method = "die", at = @At("HEAD"))
    private void onDie(DamageSource source, CallbackInfo ci) {
        if (!CarpetVPLAdditionSettings.villagerReincarnation) return;
        try {
            Villager self = (Villager) (Object) this;
            Entity killer = source.getEntity();
            if (!(killer instanceof LivingEntity livingKiller)) return;

            ItemStack weapon = livingKiller.getMainHandItem();
            if (!weapon.is(Items.WOODEN_SWORD)) return;
            if (!weapon.isEnchanted()) return;

            if (!(self.level() instanceof ServerLevel serverLevel)) return;

            self.getBrain().eraseMemory(MemoryModuleType.JOB_SITE);
            self.getBrain().eraseMemory(MemoryModuleType.POTENTIAL_JOB_SITE);

            self.getBrain().eraseMemory(MemoryModuleType.LAST_WORKED_AT_POI);

            CompoundTagValueOutput output = new CompoundTagValueOutput(
                ProblemReporter.DISCARDING,
                serverLevel.registryAccess().createSerializationContext(NbtOps.INSTANCE),
                new CompoundTag()
            );
            self.saveWithoutId(output);
            CompoundTag tag = output.buildResult();
            tag.putString("id", "minecraft:villager");

            tag.remove("Pos");
            tag.remove("Motion");
            tag.remove("Rotation");
            tag.remove("UUID");
            tag.remove("Health");
            tag.remove("Air");
            tag.remove("Fire");
            tag.remove("FallDistance");
            tag.remove("OnGround");
            tag.remove("DeathTime");
            tag.remove("HurtTime");
            tag.remove("HurtByTimestamp");
            tag.remove("PortalCooldown");
            tag.remove("FallFlying");
            tag.remove("AbsorptionAmount");
            tag.remove("Invulnerable");
            tag.remove("PersistenceRequired");
            tag.remove("CanPickUpLoot");
            tag.remove("LeftHanded");
            tag.remove("FoodLevel");
            tag.remove("ForcedAge");
            tag.remove("LastRestock");
            tag.remove("LastGossipDecay");
            tag.remove("RestocksToday");

            SpawnEggHelper.spawnVillagerEgg(self, serverLevel, tag);
        } catch (Throwable e) {
            System.err.println("[carpet-vpl-addition] VillagerReincarnation error: " + e.getMessage());
        }
    }
}
