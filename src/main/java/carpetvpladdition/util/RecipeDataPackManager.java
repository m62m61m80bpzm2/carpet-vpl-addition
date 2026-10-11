package carpetvpladdition.util;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * 26.3+ 动态配方数据包管理器（b1.16.1.2 新增）。
 *
 * 背景：26.3 重构了配方加载——RecipeManager 改为从 Registries.RECIPE 可重载注册表
 * 构建（RegistryDataLoader 用 Recipe.DIRECT_CODEC 解析 datapack JSON），
 * 26.2 及以前的 RecipeManager.prepare(ResourceManager, ProfilerFiller) 注入点被移除，
 * 因此 b1.16.1.1 曾把 5 条配方规则整体门控关闭（26.3 上无效）。
 *
 * 本类采用"动态数据包"方案恢复 26.3 支持（也是各类加配方 mod 的通用做法）：
 * 把规则对应配方 JSON 写入 <world>/datapacks/carpet-vpl-addition/，由原版
 * FolderRepositorySource 发现、reloadResources 时经 RegistryDataLoader 注册进
 * 配方注册表。规则开关 = 写入/删除对应 JSON 文件 + reloadResources，无需 mixin。
 *
 * 注意：26.3 配方数据目录为 data/&lt;ns&gt;/recipe/（单数）；26.2 及以前为 recipes/（复数）。
 * pack_format=97 为 26.3 的 data pack 版本（DetectedVersion: PackFormat.of(97, 1)）。
 */
public final class RecipeDataPackManager {

    /** 数据包目录名（world/datapacks 下的目录） */
    private static final String PACK_DIR_NAME = "carpet-vpl-addition";
    /**
     * PackRepository 中的 pack id。26.3 的 FolderRepositorySource 给世界数据包的
     * id 加 "file/" 前缀（PackLocationInfo("file/" + 目录名)），查找必须用该形式。
     */
    private static final String PACK_ID = "file/" + PACK_DIR_NAME;
    /** 26.3 data pack 版本 */
    private static final int PACK_FORMAT = 97;

    private record RuleRecipe(String rule, String fileName, String json) {
    }

    /**
     * 与 RecipeManagerMixin.DYNAMIC_RECIPES 保持一致（26.1/26.2 走 mixin 注入，
     * 26.3 走本类数据包，两套实现共用同一份配方定义语义）。
     */
    private static final List<RuleRecipe> RECIPES = List.of(
        new RuleRecipe("totemRecipe", "totem_of_undying.json", """
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
            """),
        new RuleRecipe("calciteRecipe", "calcite_from_andesite.json", """
            {
                "type": "minecraft:crafting_shapeless",
                "category": "building",
                "ingredients": ["minecraft:andesite", "minecraft:bone_meal"],
                "result": {"count": 2, "id": "minecraft:calcite"}
            }
            """),
        new RuleRecipe("tuffRecipe", "tuff_from_cobblestone.json", """
            {
                "type": "minecraft:crafting_shapeless",
                "category": "building",
                "ingredients": ["minecraft:cobblestone", "minecraft:bone_meal"],
                "result": {"count": 2, "id": "minecraft:tuff"}
            }
            """),
        new RuleRecipe("woolToString", "string_from_wool.json", """
            {
                "type": "minecraft:crafting_shapeless",
                "category": "misc",
                "ingredients": ["#minecraft:wool"],
                "result": {"count": 4, "id": "minecraft:string"}
            }
            """),
        new RuleRecipe("quartzUnpack", "quartz_from_block.json", """
            {
                "type": "minecraft:crafting_shapeless",
                "category": "misc",
                "ingredients": ["minecraft:quartz_block"],
                "result": {"count": 4, "id": "minecraft:quartz"}
            }
            """)
    );

    private RecipeDataPackManager() {
    }

    /**
     * 按 5 条配方规则的当前开关状态同步数据包内容：
     * 开启的规则写入配方 JSON，关闭的删除对应文件；任一开启时把数据包
     * 加入 PackRepository 的启用集合，全部关闭则移除。
     * 调用方随后用 repository.getSelectedIds() 执行 reloadResources。
     */
    public static void updatePackFiles(MinecraftServer server) {
        try {
            Path packRoot = server.getWorldPath(LevelResource.DATAPACK_DIR).resolve(PACK_DIR_NAME);
            Path recipeDir = packRoot.resolve("data").resolve(PACK_DIR_NAME).resolve("recipe");

            // pack.mcmeta 首次创建（幂等）
            Files.createDirectories(recipeDir);
            Path meta = packRoot.resolve("pack.mcmeta");
            if (!Files.exists(meta)) {
                // 26.3 权威格式（照抄原版内置数据包）：不写 pack_format，只用 min_format/max_format
                // （带 pack_format:97 会触发 validatePackFormatForRange 额外校验导致 pack 被跳过）
                Files.writeString(meta, "{\"pack\":{\"description\":\"Carpet VPL Addition dynamic recipes (MC 26.3+)\""
                    + ",\"min_format\":" + PACK_FORMAT
                    + ",\"max_format\":" + PACK_FORMAT + "}}", StandardCharsets.UTF_8);
            }

            boolean anyEnabled = false;
            for (RuleRecipe rr : RECIPES) {
                boolean enabled = ruleEnabled(rr.rule());
                Path jsonFile = recipeDir.resolve(rr.fileName());
                if (enabled) {
                    anyEnabled = true;
                    // 内容变化才写，避免无谓的 mtime 变动
                    if (!Files.exists(jsonFile) || !Files.readString(jsonFile, StandardCharsets.UTF_8).equals(rr.json())) {
                        Files.writeString(jsonFile, rr.json(), StandardCharsets.UTF_8);
                    }
                } else {
                    Files.deleteIfExists(jsonFile);
                }
            }

            PackRepository repo = server.getPackRepository();
            boolean currentlySelected = repo.getSelectedIds().contains(PACK_ID);
            if (anyEnabled && !currentlySelected) {
                // 新写入的 pack 需要 repository 重新扫描才能被发现
                repo.reload();
                repo.addPack(PACK_ID);
            } else if (!anyEnabled && currentlySelected) {
                repo.removePack(PACK_ID);
            }
        } catch (IOException e) {
            System.err.println("[carpet-vpl-addition] Failed to update recipe datapack: " + e);
        }
    }

    /** 世界加载完成时调用：按已保存的规则状态重建数据包并确保启用（幂等），reload 与解锁统一走 RecipeRuleObserver */
    public static void initOnServerLoadedWorlds(MinecraftServer server) {
        updatePackFiles(server);
        try {
            if (anyRecipeEnabled() && !server.getPackRepository().getSelectedIds().contains(PACK_ID)) {
                // Carpet 配置加载（observer 触发）早于世界加载完成，当时 pack 尚未写入，
                // 这里补一次发现扫描；随后的 reloadAndUnlock 会以含本包的 selectedIds 重载
                server.getPackRepository().reload();
                server.getPackRepository().addPack(PACK_ID);
            }
        } catch (Exception e) {
            System.err.println("[carpet-vpl-addition] Failed to init recipe datapack: " + e);
        }
    }

    private static boolean ruleEnabled(String rule) {
        try {
            return CarpetVPLAdditionSettings.class.getField(rule).getBoolean(null);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean anyRecipeEnabled() {
        for (RuleRecipe rr : RECIPES) {
            if (ruleEnabled(rr.rule())) {
                return true;
            }
        }
        return false;
    }

    /** 当前开启规则对应的数据包内配方文件名（供调试/日志） */
    public static List<String> enabledRecipeFiles() {
        return RECIPES.stream().filter(rr -> ruleEnabled(rr.rule())).map(RuleRecipe::fileName).toList();
    }

    /** 清理工具：列出数据包目录内全部文件（调试用） */
    public static Stream<Path> listPackFiles(MinecraftServer server) throws IOException {
        Path packRoot = server.getWorldPath(LevelResource.DATAPACK_DIR).resolve(PACK_DIR_NAME);
        if (!Files.exists(packRoot)) {
            return Stream.empty();
        }
        return Files.walk(packRoot);
    }
}
