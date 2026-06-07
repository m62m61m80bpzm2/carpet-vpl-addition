package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.SortedMap;

@Mixin(value = RecipeManager.class, priority = 16888)
public abstract class RecipeManagerMixin {
    private static final Gson GSON = new Gson();
    private static final ResourceLocation TOTEM_ID = ResourceLocation.parse("carpet-vpl-addition:totem_of_undying");

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

    @Inject(
        method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Lnet/minecraft/world/item/crafting/RecipeMap;",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/ArrayList;<init>(I)V"
        )
    )
    private void addTotemRecipe(CallbackInfoReturnable<RecipeMap> cir,
                                 @Local SortedMap<ResourceLocation, Recipe<?>> recipes) {
        if (!CarpetVPLAdditionSettings.totemRecipe) return;

        RecipeManagerAccessor accessor = (RecipeManagerAccessor) this;
        JsonObject json = GSON.fromJson(TOTEM_RECIPE_JSON, JsonObject.class);
        Recipe<?> recipe = Recipe.CODEC
            .parse(accessor.getRegistries().createSerializationContext(JsonOps.INSTANCE), json)
            .getOrThrow();
        recipes.put(TOTEM_ID, recipe);
    }
}
