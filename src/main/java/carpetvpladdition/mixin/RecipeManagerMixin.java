package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = RecipeManager.class, priority = 16888)
public abstract class RecipeManagerMixin {
    private static final Gson GSON = new Gson();
    private static final Identifier TOTEM_ID = Identifier.parse("carpet-vpl-addition:totem_of_undying");

    private static final String TOTEM_RECIPE_JSON = """
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
        """;

    @ModifyReturnValue(
        method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Lnet/minecraft/world/item/crafting/RecipeMap;",
        at = @At("RETURN")
    )
    private RecipeMap addTotemRecipe(RecipeMap original) {
        if (!CarpetVPLAdditionSettings.totemRecipe) return original;

        RecipeManagerAccessor accessor = (RecipeManagerAccessor) this;
        JsonObject json = GSON.fromJson(TOTEM_RECIPE_JSON, JsonObject.class);
        Recipe<?> recipe = Recipe.CODEC
            .parse(accessor.getRegistries().createSerializationContext(JsonOps.INSTANCE), json)
            .getOrThrow();

        ResourceKey<Recipe<?>> recipeKey = ResourceKey.create(Registries.RECIPE, TOTEM_ID);
        List<RecipeHolder<?>> all = new ArrayList<>();
        original.values().forEach(all::add);
        all.add(new RecipeHolder<>(recipeKey, recipe));
        return RecipeMap.create(all);
    }
}
