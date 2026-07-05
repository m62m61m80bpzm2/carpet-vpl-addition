package carpetvpladdition.settings;

import carpet.api.settings.Rule;

import static carpet.api.settings.RuleCategory.FEATURE;
import static carpet.api.settings.RuleCategory.SURVIVAL;

/**
 * 刷线机规则（版本门控：仅在 ≥1.21.2 时注册）。
 *
 * 刷线机漏洞（MC-59471 / MC-129055）从远古版本就存在，
 * Mojang 在 24w33a（1.21.2）中通过给 calculateState() 的 setBlock 加
 * is(Blocks.TRIPWIRE) || is(Blocks.TRIPWIRE_HOOK) 守卫修复。
 *
 * 低于 1.21.2 的版本漏洞天然存在，不需要此规则；
 * 仅当 Mod 运行在 1.21.2+ 时，此规则才注册到 /carpet 命令中，
 * 供玩家选择是否重新引入刷线机。
 */
public class CarpetVPLAdditionStringDupeSettings {
    public static final String CARPET_VPL_ADDITION = "carpet_vpl_addition";

    @Rule(
        categories = {CARPET_VPL_ADDITION, FEATURE, SURVIVAL}
    )
    public static boolean stringDupe = false;
}
