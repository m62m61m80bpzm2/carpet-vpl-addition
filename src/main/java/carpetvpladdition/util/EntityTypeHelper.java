package carpetvpladdition.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jspecify.annotations.Nullable;

/**
 * 跨版本实体类型查找工具（b1.14.3.3 新增）。
 *
 * 26.1 系列中实体类型常量位于 EntityType 类（EntityType.VILLAGER），
 * 26.2 起移入新类 EntityTypes（EntityTypes.VILLAGER）。
 * 为避免常量位置差异导致的 NoSuchFieldError / 编译失败，统一走注册表
 * BuiltInRegistries.ENTITY_TYPE.getValue(Identifier) 按 ID 查找，
 * 该 API 在 26.1 ~ 26.2 中签名一致。
 */
public final class EntityTypeHelper {

    private EntityTypeHelper() {
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends Entity> EntityType<T> get(String id) {
        return (EntityType<T>) BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace(id));
    }
}
