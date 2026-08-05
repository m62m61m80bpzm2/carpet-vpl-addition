package carpetvpladdition.util;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.lang.reflect.Method;

/**
 * 刷怪蛋工具。
 *
 * 兼容性说明：1.21.9 起 {@code DataComponents.ENTITY_DATA} 的类型从
 * {@code CustomData} 变为 {@code TypedEntityData<EntityType<?>>}，直接编译期调用
 * 无法同时兼容 1.21.6~1.21.8 与 1.21.9+，因此这里全部通过反射设置组件值，
 * 保证一个 jar 通吃 1.21.6 ~ 1.21.10。
 */
public class SpawnEggHelper {
    private static Method setMethod;
    private static Method customDataOfMethod;
    private static Method typedEntityDataOfMethod;
    private static Boolean typedEntityDataAvailable;

    public static void spawnVillagerEgg(Villager villager, ServerLevel serverLevel, CompoundTag tag) {
        ItemStack egg = new ItemStack(Items.VILLAGER_SPAWN_EGG);
        setEntityData(egg, tag);

        ItemEntity itemEntity = new ItemEntity(
            serverLevel,
            villager.getX(), villager.getY(), villager.getZ(),
            egg
        );
        serverLevel.addFreshEntity(itemEntity);
    }

    /** 反射写入 ENTITY_DATA 组件，兼容 CustomData（1.21.6~1.21.8）与 TypedEntityData（1.21.9+） */
    private static void setEntityData(ItemStack egg, CompoundTag tag) {
        try {
            if (isTypedEntityDataAvailable()) {
                Object data = getTypedEntityDataOf().invoke(null, getVillagerEntityType(), tag);
                getSetMethod().invoke(egg, DataComponents.ENTITY_DATA, data);
            } else {
                Object data = getCustomDataOf().invoke(null, tag);
                getSetMethod().invoke(egg, DataComponents.ENTITY_DATA, data);
            }
        } catch (Exception e) {
            System.err.println("[carpet-vpl-addition] Failed to set entity data: " + e);
        }
    }

    /** ItemStack.set(DataComponentType, Object)（泛型擦除后） */
    private static Method getSetMethod() throws NoSuchMethodException {
        if (setMethod == null) {
            setMethod = ItemStack.class.getMethod("set", DataComponentType.class, Object.class);
        }
        return setMethod;
    }

    /** CustomData.of(CompoundTag) */
    private static Method getCustomDataOf() throws Exception {
        if (customDataOfMethod == null) {
            Class<?> customData = Class.forName("net.minecraft.world.item.component.CustomData");
            customDataOfMethod = customData.getMethod("of", CompoundTag.class);
        }
        return customDataOfMethod;
    }

    /** TypedEntityData.of(EntityType, CompoundTag) */
    private static Method getTypedEntityDataOf() throws Exception {
        if (typedEntityDataOfMethod == null) {
            Class<?> typedEntityData = Class.forName("net.minecraft.world.item.component.TypedEntityData");
            Class<?> entityType = Class.forName("net.minecraft.world.entity.EntityType");
            typedEntityDataOfMethod = typedEntityData.getMethod("of", entityType, CompoundTag.class);
        }
        return typedEntityDataOfMethod;
    }

    /** EntityType.VILLAGER 静态字段 */
    private static Object getVillagerEntityType() throws Exception {
        Class<?> entityType = Class.forName("net.minecraft.world.entity.EntityType");
        return entityType.getField("VILLAGER").get(null);
    }

    private static boolean isTypedEntityDataAvailable() {
        if (typedEntityDataAvailable == null) {
            try {
                Class.forName("net.minecraft.world.item.component.TypedEntityData");
                typedEntityDataAvailable = Boolean.TRUE;
            } catch (ClassNotFoundException e) {
                typedEntityDataAvailable = Boolean.FALSE;
            }
        }
        return typedEntityDataAvailable;
    }
}
