package carpetvpladdition.settings;

import carpet.api.settings.Rule;
import static carpet.api.settings.RuleCategory.FEATURE;
import static carpet.api.settings.RuleCategory.SURVIVAL;

public class CarpetVPLAdditionSettings {
    public static final String CARPET_VPL_ADDITION = "carpet_vpl_addition";

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL},
        options = {"20", "40", "60", "80", "100", "200", "500", "1000"},
        strict = false
    )
    public static String maxPlayerHealth = "20";

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL},
        options = {"2.0", "4.0", "6.0", "10.0", "20.0", "50.0"},
        strict = false
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
    public static boolean stackableBucket = false;

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
        strict = false
    )
    public static String maxAir = "300";

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL},
        options = {"0", "10", "20", "30"},
        strict = false
    )
    public static String maxArmor = "0";

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL},
        options = {"20", "40", "60", "100", "200"},
        strict = false
    )
    public static String maxSaturation = "20";

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE}
    )
    public static boolean stackableMusicDisc = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE}
    )
    public static boolean stackableMilkBucket = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE}
    )
    public static boolean stackablePowderSnowBucket = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE}
    )
    public static boolean stackableEnderPearl = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE}
    )
    public static boolean stackableSign = false;

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE},
        options = {"1", "16", "32", "64"},
        strict = false
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
}
