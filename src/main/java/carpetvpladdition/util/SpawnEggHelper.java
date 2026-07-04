package carpetvpladdition.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

public class SpawnEggHelper {
    public static void spawnVillagerEgg(Villager villager, ServerLevel serverLevel, CompoundTag tag) {
        ItemStack egg = new ItemStack(Items.VILLAGER_SPAWN_EGG);
        egg.set(DataComponents.ENTITY_DATA, CustomData.of(tag));

        ItemEntity itemEntity = new ItemEntity(
            serverLevel,
            villager.getX(), villager.getY(), villager.getZ(),
            egg
        );
        serverLevel.addFreshEntity(itemEntity);
    }
}
