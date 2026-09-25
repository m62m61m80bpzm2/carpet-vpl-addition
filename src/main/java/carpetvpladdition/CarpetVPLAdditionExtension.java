package carpetvpladdition;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.api.settings.SettingsManager;
import carpetvpladdition.settings.CarpetVPLAdditionSettings;
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
            // 与 26.1-26.2 分支同步的修复：Carpet 的 ParsedRule.set 顺序是
            // 先跑 validator → 再写 @Rule 静态字段 → 最后 notifyRuleChanged，
            // 故挂在 validator 里的 syncNumericCaches 读字段必然拿到旧值，
            // 游戏中改数值规则会"看起来没效果"。观察者在校后触发，读到的才是新值。
            SettingsManager.registerGlobalRuleObserver(
                (source, rule, userInput) -> CarpetVPLAdditionSettings.syncNumericCaches());
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
