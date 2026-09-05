package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态配方注册（totemRecipe / calciteRecipe / tuffRecipe / woolToString / quartzUnpack）。
 *
 * 在 RecipeManager.prepare 构建配方表时按规则开关附加自定义配方，
 * 关闭规则后不注册；配方 JSON 与原版 data 包格式一致（1.21.2+ 格式：
 * ingredient 为字符串或 "#tag"，result 用 {"count": N, "id": "..."}）。
 */
@Mixin(value = RecipeManager.class, priority = 16888)
public abstract class RecipeManagerMixin {
    private static final Gson GSON = new Gson();

    /** 动态配方定义：规则字段、配方 ID、配方 JSON */
    private record DynamicRecipe(String ruleName, Identifier id, String json) {
        boolean enabled() {
            try {
                return CarpetVPLAdditionSettings.class.getField(ruleName).getBoolean(null);
            } catch (Exception e) {
                return false;
            }
        }
    }

    private static final List<DynamicRecipe> DYNAMIC_RECIPES = List.of(
        // 不死图腾配方：金胡萝卜+附魔金苹果+绿宝石+金块（历史配方）
        new DynamicRecipe("totemRecipe",
            Identifier.parse("carpet-vpl-addition:totem_of_undying"), """
            {
                "type": "minecraft:crafting_shaped",
                "category": "equipment",
                "pattern": ["ABA","CDC","DDD"],
                "key": {
                    "A": "minecraft:golden_carrot",
                    "B": "minecraft:enchanted_golden_apple",
                    "C": "minecraft:emerald",
                    "D": "minecraft:gold_block"
                },
                "result": {"id": "minecraft:totem_of_undying", "count": 1}
            }
            """),
        // 方解石合成：安山岩+骨粉 → 2（材料全部可再生，1:1 无漏洞）
        new DynamicRecipe("calciteRecipe",
            Identifier.parse("carpet-vpl-addition:calcite_from_andesite"), """
            {
                "type": "minecraft:crafting_shapeless",
                "category": "building",
                "ingredients": ["minecraft:andesite", "minecraft:bone_meal"],
                "result": {"count": 2, "id": "minecraft:calcite"}
            }
            """),
        // 凝灰岩合成：圆石+骨粉 → 2（材料全部可再生，与方解石配方成对）
        new DynamicRecipe("tuffRecipe",
            Identifier.parse("carpet-vpl-addition:tuff_from_cobblestone"), """
            {
                "type": "minecraft:crafting_shapeless",
                "category": "building",
                "ingredients": ["minecraft:cobblestone", "minecraft:bone_meal"],
                "result": {"count": 2, "id": "minecraft:tuff"}
            }
            """),
        // 羊毛分解：任意颜色羊毛 → 4 线（#minecraft:wool 标签 17 色通吃，
        // 与原版 4 线→1 羊毛完全对称可逆，无刷取漏洞）
        new DynamicRecipe("woolToString",
            Identifier.parse("carpet-vpl-addition:string_from_wool"), """
            {
                "type": "minecraft:crafting_shapeless",
                "category": "misc",
                "ingredients": ["#minecraft:wool"],
                "result": {"count": 4, "id": "minecraft:string"}
            }
            """),
        // 石英块分解：石英块 → 4 下界石英（与原版 4:1 对称可逆；
        // 仅支持普通石英块，石英柱/雕纹/砖 1:1 分解会产生凭空溢出漏洞）
        new DynamicRecipe("quartzUnpack",
            Identifier.parse("carpet-vpl-addition:quartz_from_block"), """
            {
                "type": "minecraft:crafting_shapeless",
                "category": "misc",
                "ingredients": ["minecraft:quartz_block"],
                "result": {"count": 4, "id": "minecraft:quartz"}
            }
            """)
    );

    @ModifyReturnValue(
        method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Lnet/minecraft/world/item/crafting/RecipeMap;",
        at = @At("RETURN")
    )
    private RecipeMap addDynamicRecipes(RecipeMap original) {
        List<RecipeHolder<?>> all = new ArrayList<>();
        original.values().forEach(all::add);

        for (DynamicRecipe entry : DYNAMIC_RECIPES) {
            if (!entry.enabled()) continue;
            try {
                JsonObject json = GSON.fromJson(entry.json(), JsonObject.class);
                Recipe<?> recipe = Recipe.CODEC
                    .parse(((RecipeManagerAccessor) this).getRegistries().createSerializationContext(JsonOps.INSTANCE), json)
                    .getOrThrow();
                all.add(new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, entry.id()), recipe));
            } catch (Exception e) {
                // 单条配方解析失败不影响其余配方与原版配方表
                System.err.println("[carpet-vpl-addition] Failed to register recipe " + entry.id() + ": " + e.getMessage());
            }
        }
        return RecipeMap.create(all);
    }
}
