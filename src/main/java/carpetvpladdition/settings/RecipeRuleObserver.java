package carpetvpladdition.settings;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Validator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 配方规则观察器。
 *
 * 规则变更时：
 *  1. 异步触发服务端资源重载（reloadResources），使 RecipeManagerMixin 注册的动态配方
 *     立即进入/退出 RecipeMap；原版重载完成会调用 PlayerList.reloadResources() 向
 *     所有玩家广播 ClientboundUpdateRecipesPacket，合成台即刻可用；
 *  2. 重载完成后把开启的动态配方立即"解锁"到所有在线玩家的配方书
 *     （ServerRecipeBook.addRecipes：解锁 + 新配方高亮 + 发送配方书包），
 *     玩家无需 /reload 或重新登录就能在配方书里看到并使用。
 */
public class RecipeRuleObserver extends Validator<Boolean> {
    // 防重复执行标志
    private static boolean pendingReload = false;

    /**
     * 动态配方清单：规则字段名 -> 配方注册 ID（命名空间 carpet-vpl-addition）。
     * 必须与 RecipeManagerMixin.DYNAMIC_RECIPES 保持一致。
     */
    private static final Map<String, String> DYNAMIC_RECIPES = Map.of(
        "totemRecipe", "totem_of_undying",
        "calciteRecipe", "calcite_from_andesite",
        "tuffRecipe", "tuff_from_cobblestone",
        "woolToString", "string_from_wool",
        "quartzUnpack", "quartz_from_block"
    );

    @Override
    public Boolean validate(CommandSourceStack source, CarpetRule<Boolean> rule, Boolean newValue, String userInput) {
        Boolean oldValue = rule.value();
        if (oldValue != newValue) {
            onValueChange(source);
        }
        return newValue;
    }

    private void onValueChange(CommandSourceStack source) {
        if (source == null || source.getServer() == null || pendingReload) {
            return;
        }
        pendingReload = true;
        MinecraftServer server = source.getServer();
        // 异步重载配方，避免在主线程同步执行完整 reloadResources
        server.executeIfPossible(() -> {
            server.reloadResources(server.getPackRepository().getSelectedIds())
                // 重载完成（RecipeMap 已更新且原版已向玩家广播配方包）后，解锁配方书
                .whenComplete((unused, throwable) -> server.execute(() -> {
                    try {
                        unlockDynamicRecipes(server);
                    } finally {
                        pendingReload = false;
                    }
                }));
        });
    }

    /** 把当前开启的动态配方立即解锁到所有在线玩家的配方书 */
    private static void unlockDynamicRecipes(MinecraftServer server) {
        RecipeManager recipeManager = server.getRecipeManager();
        List<RecipeHolder<?>> toUnlock = new ArrayList<>();
        for (Map.Entry<String, String> entry : DYNAMIC_RECIPES.entrySet()) {
            // 只解锁当前开启的规则对应的配方
            boolean enabled;
            try {
                enabled = CarpetVPLAdditionSettings.class.getField(entry.getKey()).getBoolean(null);
            } catch (Exception e) {
                enabled = false;
            }
            if (!enabled) {
                continue;
            }
            Identifier id = Identifier.parse("carpet-vpl-addition:" + entry.getValue());
            recipeManager.byKey(ResourceKey.create(Registries.RECIPE, id))
                .filter(holder -> !holder.value().isSpecial())
                .ifPresent(toUnlock::add);
        }
        if (toUnlock.isEmpty()) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            // 解锁 + 新配方高亮 + 发送配方书包，客户端立即显示
            player.getRecipeBook().addRecipes(toUnlock, player);
        }
    }
}
