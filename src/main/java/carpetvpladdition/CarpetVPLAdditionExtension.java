package carpetvpladdition;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import carpetvpladdition.settings.CarpetVPLAdditionStringDupeSettings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
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

    // 版本检测缓存
    private static Boolean isAtLeast1_21_2 = null;

    /**
     * 检测当前 MC 版本是否 ≥1.21.2。
     * 刷线机漏洞在 24w33a / 1.21.2 被修复，≥此版本才需要 stringDupe 规则。
     */
    private static boolean isVersionAtLeast1_21_2() {
        if (isAtLeast1_21_2 != null) return isAtLeast1_21_2;
        try {
            var minecraft = FabricLoader.getInstance().getModContainer("minecraft");
            if (minecraft.isPresent()) {
                String version = minecraft.get().getMetadata().getVersion().getFriendlyString();
                if (version.startsWith("26")) {
                    isAtLeast1_21_2 = true;
                    return true;
                }
                if (version.startsWith("1.21.")) {
                    String[] parts = version.split("\\.");
                    if (parts.length >= 3) {
                        int minor = Integer.parseInt(parts[2].replaceAll("[^0-9].*", ""));
                        isAtLeast1_21_2 = minor >= 2;
                        return isAtLeast1_21_2;
                    }
                }
            }
        } catch (Exception ignored) {}
        isAtLeast1_21_2 = false;
        return false;
    }

    @Override
    public void onGameStarted() {
        try {
            CarpetServer.settingsManager.parseSettingsClass(CarpetVPLAdditionSettings.class);
            // 初始化所有字符串规则的数值缓存
            CarpetVPLAdditionSettings.syncNumericCaches();

            // 刷线机漏洞在 ≥1.21.2（24w33a）被修复，
            // 仅在这些版本注册 stringDupe 规则（低于此版本漏洞天然存在，无需规则）
            if (isVersionAtLeast1_21_2()) {
                CarpetServer.settingsManager.parseSettingsClass(CarpetVPLAdditionStringDupeSettings.class);
            }
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

            AttributeInstance armorAttr = player.getAttribute(Attributes.ARMOR);
            if (armorAttr != null) {
                armorAttr.setBaseValue(CarpetVPLAdditionSettings.maxArmorCached);
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
