package carpetvpladdition.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TypedEntityData;

public class SpawnEggHelper {
    public static void spawnVillagerEgg(Villager villager, ServerLevel serverLevel, CompoundTag tag) {
        ItemStack egg = new ItemStack(Items.VILLAGER_SPAWN_EGG);
        EntityType<Villager> villagerType = EntityTypeHelper.get("villager");
        if (villagerType != null) {
            egg.set(DataComponents.ENTITY_DATA, TypedEntityData.of(villagerType, tag));
        }

        ItemEntity itemEntity = new ItemEntity(
            serverLevel,
            villager.getX(), villager.getY(), villager.getZ(),
            egg
        );
        serverLevel.addFreshEntity(itemEntity);
    }
}
