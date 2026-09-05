package carpetvpladdition.settings;

import carpet.api.settings.Rule;
import carpet.api.settings.CarpetRule;
import carpet.api.settings.Validator;
import carpetvpladdition.CarpetVPLAdditionExtension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import static carpet.api.settings.RuleCategory.FEATURE;
import static carpet.api.settings.RuleCategory.SURVIVAL;

public class CarpetVPLAdditionSettings {
    public static final String CARPET_VPL_ADDITION = "carpet_vpl_addition";

    // ========== 数值字段缓存 ==========
    // 避免热路径上反复 Integer.parseInt / Float.parseFloat
    public static int observerTickDelayCached = 2;
    public static int maxAirCached = 300;
    public static float furnaceXpMultiplierCached = 1.0f;
    public static int hopperMinecartStackSizeCached = 1;
    public static int maxPlayerHealthCached = 20;
    public static double playerAttackDamageCached = 2.0;
    public static float cactusGrowthMultiplierCached = 1.0f;

    /** 将所有字符串规则解析到缓存字段（含最小值/上限保护，防止玩家输入 0 或负数破坏机制） */
    public static void syncNumericCaches() {
        try { observerTickDelayCached = Math.max(1, Integer.parseInt(observerTickDelay)); }
        catch (NumberFormatException e) { observerTickDelayCached = 2; }

        try { maxAirCached = Math.max(1, Integer.parseInt(maxAir)); }
        catch (NumberFormatException e) { maxAirCached = 300; }

        try { furnaceXpMultiplierCached = Math.max(0.0f, Float.parseFloat(furnaceXpMultiplier)); }
        catch (NumberFormatException e) { furnaceXpMultiplierCached = 1.0f; }

        try { hopperMinecartStackSizeCached = Math.min(99, Math.max(1, Integer.parseInt(hopperMinecartStackSize))); }
        catch (NumberFormatException e) { hopperMinecartStackSizeCached = 1; }

        try { maxPlayerHealthCached = Math.max(1, Integer.parseInt(maxPlayerHealth)); }
        catch (NumberFormatException e) { maxPlayerHealthCached = 20; }

        try { playerAttackDamageCached = Math.max(0.0, Double.parseDouble(playerAttackDamage)); }
        catch (NumberFormatException e) { playerAttackDamageCached = 2.0; }

        try { cactusGrowthMultiplierCached = Math.max(0.0f, Float.parseFloat(cactusGrowthMultiplier)); }
        catch (NumberFormatException e) { cactusGrowthMultiplierCached = 1.0f; }
    }

    // ========== 通用验证器：规则变更时刷新缓存 ==========
    public static class CacheRefreshValidator extends Validator<String> {
        @Override
        public String validate(CommandSourceStack source, CarpetRule<String> rule, String newValue, String userInput) {
            syncNumericCaches();
            return newValue;
        }
    }

    /** 属性规则变更验证器：刷新缓存 + 即时应用到所有在线玩家 */
    public static class AttributeRefreshValidator extends Validator<String> {
        @Override
        public String validate(CommandSourceStack source, CarpetRule<String> rule, String newValue, String userInput) {
            syncNumericCaches();
            // 即时重新应用属性到所有在线玩家
            if (source != null) {
                MinecraftServer server = source.getServer();
                if (server != null) {
                    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                        CarpetVPLAdditionExtension.applyAttributes(player);
                    }
                }
            }
            return newValue;
        }
    }

    // ========== 规则定义 ==========

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL},
        options = {"20", "40", "60", "80", "100", "200", "500", "1000"},
        strict = false,
        validators = AttributeRefreshValidator.class
    )
    public static String maxPlayerHealth = "20";

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL},
        options = {"2.0", "4.0", "6.0", "10.0", "20.0", "50.0"},
        strict = false,
        validators = AttributeRefreshValidator.class
    )
    public static String playerAttackDamage = "2.0";

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL},
        validators = RecipeRuleObserver.class
    )
    public static boolean totemRecipe = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE}
    )
    public static boolean stackableTotem = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE}
    )
    public static boolean stackableLavaBucket = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE}
    )
    public static boolean stackableGlassBottle = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE}
    )
    public static boolean stackableWaterBucket = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL},
        options = {"300", "600", "1200", "2400"},
        strict = false,
        validators = CacheRefreshValidator.class
    )
    public static String maxAir = "300";

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE},
        options = {"1", "16", "32", "64"},
        strict = false,
        validators = CacheRefreshValidator.class
    )
    public static String hopperMinecartStackSize = "1";

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE}
    )
    public static boolean stackablePotion = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE}
    )
    public static boolean stackableStew = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE}
    )
    public static boolean stackableCake = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL}
    )
    public static boolean villagerGolem = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL}
    )
    public static boolean villagerAttraction = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL}
    )
    public static boolean fastLeafDecay = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL}
    )
    public static boolean fixedXpPerLevel = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL},
        options = {"1.0", "2.0", "5.0", "10.0", "50.0"},
        strict = false,
        validators = CacheRefreshValidator.class
    )
    public static String furnaceXpMultiplier = "1.0";

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL}
    )
    public static boolean tridentVoidReturn = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL}
    )
    public static boolean villagerReincarnation = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL}
    )
    public static boolean bedrockSugarcaneBonemeal = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL}
    )
    public static boolean bedrockPortalZombiePigman = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL}
    )
    public static boolean bedrockPushableFurnace = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL}
    )
    public static boolean protectImmatureCrops = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL},
        options = {"1", "2", "4", "8", "16"},
        strict = false,
        validators = CacheRefreshValidator.class
    )
    public static String observerTickDelay = "2";

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL}
    )
    public static boolean disableSnow = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL}
    )
    public static boolean disableKelpGrowth = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL}
    )
    public static boolean stringDupe = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL}
    )
    public static boolean villagerNoPriceOnAttack = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL},
        options = {"1.0", "2.0", "4.0", "8.0", "16.0"},
        strict = false,
        validators = CacheRefreshValidator.class
    )
    public static String cactusGrowthMultiplier = "1.0";

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL}
    )
    public static boolean cactusBoneMeal = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE}
    )
    public static boolean pickBlockNbt = true;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL}
    )
    public static boolean beaconUnifiedPPUpdate = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL}
    )
    public static boolean noZombieHorseSpawn = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE}
    )
    public static boolean anvilNoDamage = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE}
    )
    public static boolean calciteRecipe = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE}
    )
    public static boolean tuffRecipe = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE}
    )
    public static boolean woolToString = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE}
    )
    public static boolean quartzUnpack = false;
}
