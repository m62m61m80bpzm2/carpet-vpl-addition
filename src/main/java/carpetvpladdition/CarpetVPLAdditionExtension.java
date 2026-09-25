package carpetvpladdition;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.api.settings.SettingsManager;
import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import carpetvpladdition.util.BeaconPPUpdateManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CarpetVPLAdditionExtension implements CarpetExtension {
    private static final Gson GSON = new Gson();
    // 翻译缓存，避免每次请求都重新读资源流
    private final Map<String, Map<String, String>> translationCache = new ConcurrentHashMap<>();

    @Override
    public void onGameStarted() {
        try {
            CarpetServer.settingsManager.parseSettingsClass(CarpetVPLAdditionSettings.class);
            // 初始化所有字符串规则的数值缓存
            CarpetVPLAdditionSettings.syncNumericCaches();
            // b1.15.5 修复"改了规则没反应"：Carpet 的 ParsedRule.set 顺序是先跑 validator、
            // 后 field.set、最后才 notifyRuleChanged（javap carpet 26.1 确认），所以挂在 validator 里的
            // syncNumericCaches 读到的一定是旧字段值，缓存永远慢一步。观察者在校后才触发，
            // 此时读静态字段拿到的就是刚写入的新值。数值规则与 stackable* 聚合开关都靠它兜底。
            SettingsManager.registerGlobalRuleObserver(
                (source, rule, userInput) -> CarpetVPLAdditionSettings.syncNumericCaches());
            // 注册信标统一 PP 更新：信标追踪 + 每 20GT 统一触发（规则 beaconUnifiedPPUpdate）
            BeaconPPUpdateManager.register();
        } catch (Exception e) {
            System.err.println("[carpet-vpl-addition] Failed to register settings: " + e.getMessage());
        }
    }

    @Override
    public void onPlayerLoggedIn(ServerPlayer player) {
        applyAttributes(player);
    }

    public static void applyAttributes(ServerPlayer player) {
        try {
            AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
            if (healthAttr != null) {
                healthAttr.setBaseValue(CarpetVPLAdditionSettings.maxPlayerHealthCached);
                player.setHealth(Math.min(player.getHealth(), (float) CarpetVPLAdditionSettings.maxPlayerHealthCached));
            }

            AttributeInstance damageAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damageAttr != null) {
                damageAttr.setBaseValue(CarpetVPLAdditionSettings.playerAttackDamageCached);
            }

        } catch (Exception e) {
            System.err.println("[carpet-vpl-addition] Failed to apply attributes: " + e.getMessage());
        }
    }

    @Override
    public Map<String, String> canHasTranslations(String lang) {
        Map<String, String> cached = translationCache.get(lang);
        if (cached != null) return cached;

        Map<String, String> result = loadTranslation(lang);
        translationCache.put(lang, result);
        return result;
    }

    private Map<String, String> loadTranslation(String lang) {
        InputStream langFile = null;
        try {
            langFile = CarpetVPLAdditionExtension.class.getResourceAsStream("/assets/carpet-vpl-addition/lang/" + lang + ".json");
            if (langFile == null) {
                langFile = CarpetVPLAdditionExtension.class.getResourceAsStream("/assets/carpet-vpl-addition/lang/" + lang.replace('-', '_') + ".json");
            }
            if (langFile == null) {
                return Collections.emptyMap();
            }
            String jsonData = new String(langFile.readAllBytes(), StandardCharsets.UTF_8);
            return GSON.fromJson(jsonData, new TypeToken<Map<String, String>>() {}.getType());
        } catch (Exception e) {
            System.err.println("[carpet-vpl-addition] Failed to load translations for " + lang + ": " + e.getMessage());
            return Collections.emptyMap();
        } finally {
            if (langFile != null) {
                try { langFile.close(); } catch (Exception ignored) {}
            }
        }
    }

    @Override
    public String version() {
        return "carpet-vpl-addition";
    }
}
