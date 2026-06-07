package carpetvpladdition;

import carpet.CarpetExtension;
import carpet.CarpetServer;
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

public class CarpetVPLAdditionExtension implements CarpetExtension {
    private static final Gson GSON = new Gson();

    @Override
    public void onGameStarted() {
        try {
            CarpetServer.settingsManager.parseSettingsClass(CarpetVPLAdditionSettings.class);
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
            int health = parseHealth();
            AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
            if (healthAttr != null) {
                healthAttr.setBaseValue(health);
                player.setHealth(Math.min(player.getHealth(), (float) health));
            }

            double damage = parseDamage();
            AttributeInstance damageAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damageAttr != null) {
                damageAttr.setBaseValue(damage);
            }

            double armor = parseArmor();
            AttributeInstance armorAttr = player.getAttribute(Attributes.ARMOR);
            if (armorAttr != null) {
                armorAttr.setBaseValue(armor);
            }
        } catch (Exception e) {
            System.err.println("[carpet-vpl-addition] Failed to apply attributes: " + e.getMessage());
        }
    }

    private static int parseHealth() {
        try {
            return Integer.parseInt(CarpetVPLAdditionSettings.maxPlayerHealth);
        } catch (NumberFormatException e) {
            return 20;
        }
    }

    private static double parseDamage() {
        try {
            return Double.parseDouble(CarpetVPLAdditionSettings.playerAttackDamage);
        } catch (NumberFormatException e) {
            return 2.0;
        }
    }

    private static double parseArmor() {
        try {
            return Double.parseDouble(CarpetVPLAdditionSettings.maxArmor);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    @Override
    public Map<String, String> canHasTranslations(String lang) {
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
