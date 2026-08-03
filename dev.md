# Carpet VPL Addition - 开发文档

本文档面向**开发者**（修改本 Mod 源码 / 参与开发）。
如果你想安装或使用这个 Mod，请查看 → [README.md](./README.md)（使用者文档）。

## 目录

- [版本规则](#版本规则)
- [技术栈与环境](#技术栈与环境)
- [构建与发布](#构建与发布)
- [项目结构](#项目结构)
- [规则注册机制](#规则注册机制)
- [跨版本兼容](#跨版本兼容)
- [性能优化](#性能优化)
- [修复记录（变更日志）](#修复记录变更日志)
- [已知问题](#已知问题)

## 版本号

- 当前版本：**1.14.0**
- 规则：**每次修改后版本号 +1**（如 1.14.0 → 1.14.1）。
- git 提交信息 = 当前版本号。

## 技术栈与环境

| 项目 | 版本 |
|---|---|
| Minecraft | 26.2 |
| Java（运行/编译） | 25 |
| Fabric Loader | 0.19.3 |
| Fabric API | 0.154.2+26.2 |
| Carpet | 26.2+v260616 |
| fabric-loom | 1.17.11 |
| mappings | Mojang official |
| Gradle | 9.5.1（wrapper） |

## 构建与发布

```bash
./gradlew build --no-daemon
```

产物位于 `build/libs/`：
- `[vpl26.2]carpet-vpl-addition-<version>.jar` — 发布用 jar（已 remap 到 intermediary）
- `[vpl26.2]carpet-vpl-addition-<version>-sources.jar` — 源码 jar

> 注意：若出现“卡住不动”的假死，通常是上次超时被杀掉的 Gradle daemon 遗留了 Loom 缓存锁。
> 用 `--no-daemon` 构建，或先执行 `./gradlew --stop` 清理。

### 发布路径

正式发布时复制到 `D:\Ddesketp\agent\my-mods\`，不要覆盖旧文件。

### Gradle 关键配置（gradle.properties / build.gradle）

```properties
minecraft_version=26.2
loader_version=0.19.3
fabric_version=0.154.2+26.2
carpet_version=26.2+v260616
mod_version=1.14.0
archives_base_name=[vpl26.2]carpet-vpl-addition
```

```groovy
// build.gradle
sourceCompatibility = JavaVersion.VERSION_21
it.options.release = 21      // 即使目标 MC 用 Java 25，我们也以 Java 21 字节码编译
```

### 依赖声明（fabric.mod.json）

```json
"depends": {
  "fabricloader": ">=0.19.3",
  "minecraft": ">=26.2 <26.3",
  "java": ">=25",
  "fabric-api": "*",
  "carpet": ">=26.2"
}
```

## 项目结构

```
src/main/java/carpetvpladdition/
├── CarpetVPLAdditionMod.java         # Mod 入口（ModInitializer）
├── CarpetVPLAdditionExtension.java   # CarpetExtension 实现（注册规则/命令/翻译）
├── mixin/                            # Mixin 类
│   ├── VersionedMixinPlugin.java     # 版本门控插件（IMixinConfigPlugin）
│   ├── PlayerAttributeMixin.java       # 玩家属性（血量/攻击/护甲）
│   ├── StackableItemMixin.java         # 物品堆叠
│   ├── LivingEntityMixin.java          # 最大氧气值
│   ├── FoodDataMixin.java              # 最大饱食度
│   ├── VillagerGolemMixin.java         # 村民傀儡
│   ├── VillagerAttractionMixin.java    # 村民吸引
│   ├── FastLeafDecayMixin.java         # 快速树叶腐烂
│   ├── FixedXpPerLevelMixin.java       # 固定升级经验
│   ├── FurnaceXpControllerMixin.java   # 熔炉经验倍率
│   ├── TridentVoidReturnMixin.java     # 三叉戟虚空返回
│   ├── VillagerReincarnationMixin.java # 转世村民
│   ├── SugarcaneBonemealMixin.java     # 甘蔗骨粉催熟
│   ├── PortalZombiePigmanMixin.java    # 地狱门刷僵尸猪人
│   ├── PushableFurnaceMixin.java       # 可推动熔炉
│   ├── ImmatureCropProtectionMixin.java# 保护未成熟农作物
│   ├── ObserverTickControlMixin.java   # 侦测器触发延迟
│   ├── NoSnowAccumulationMixin.java    # 禁止下雪
│   ├── NoKelpGrowthMixin.java          # 禁止海带生长
│   ├── TripwireHookBlockStringDupeMixin.java # 刷线机（calculateState 类型守卫）
│   ├── TripWireBlockStringDupeMixin.java    # 刷线机（updateSource 连通性）
│   ├── VillagerNoPriceOnAttackMixin.java    # 攻击不涨价
│   ├── CactusGrowthMixin.java            # 仙人掌生长速度倍率
│   ├── CactusBonemealMixin.java         # 仙人掌骨粉催熟
│   ├── PickBlockNbtMixin.java            # 中键复制方块 NBT 修复
│   └── RecipeManagerAccessor/RecipeManagerMixin.java # 不死图腾配方
├── settings/
│   ├── CarpetVPLAdditionSettings.java    # 规则定义 + 数值缓存
│   └── RecipeRuleObserver.java           # 配方规则验证器
└── util/
    ├── CompoundTagValueOutput.java   # NBT 输出工具
    └── SpawnEggHelper.java           # 刷怪蛋工具
```

## 规则注册机制

### 入口与生命周期

1. `CarpetVPLAdditionMod`（`ModInitializer`）→ `CarpetServer.manageExtension(this)`
2. `CarpetVPLAdditionExtension.onGameStarted()` → `CarpetServer.settingsManager.parseSettingsClass(CarpetVPLAdditionSettings.class)`
3. `CarpetServer` 在启动阶段自动扫描 `@Rule` 字段注册，并生成 `/carpet <规则>` 命令。

### 添加一条新规则

1. 在 `CarpetVPLAdditionSettings` 添加一个 `@Rule` 标注的字段：
   ```java
   @Rule(categories = {CARPET_VPL_ADDITION, FEATURE})
   public static boolean 新规则名 = false;
   ```
2. 如需翻译，在 `assets/carpet-vpl-addition/lang/{en_us,zh_cn}.json` 添加：
   ```json
   "carpet.rule.新规则名.name": "..."
   "carpet.rule.新规则名.desc": "..."
   ```
3.（可选）字符串数值规则需在 `syncNumericCaches()` 解析到 `...Cached` 字段，并配置 Validator。

### 依赖 Carpet 版本说明

经对照 Carpet 26.2 的 jar 验证，`@Rule` 注解、`SettingsManager.parseSettingsClass`、`CarpetExtension.onGameStarted` 等 API 与 1.21.8 完全一致，注册方式未变化。

## 跨版本兼容

通过 `VersionedMixinPlugin`（实现 `IMixinConfigPlugin`）按 MC 版本动态启用/禁用某些 mixin。

| MC 版本 | 禁用 / 说明 |
|---|---|
| 1.21.0-1.21.1 | 禁用 VillagerGolem / PortalZombiePigman / Tripwire 系列（依赖 EntitySpawnReason 等） |
| 1.21.2-1.21.5 | 禁用 VillagerReincarnation |
| 1.21.6-1.21.10 | 全部启用 |
| 1.21.11 | **不受支持**（Mojang mappings 大重构，见下文） |
| 26.x | 全部启用 |

> 当前正式构建目标为 **26.2**；其余版本仅在本分支的历史中存在。

## 性能优化

1. **数值缓存**：所有字符串规则在变更时解析一次并存到 `...Cached` 字段，避免热路径反复 `parseInt`/`parseFloat`。
2. **StackableItem**：快速跳过门 + 缓存。
3. **FastLeafDecay**：delay 从 0 改为 1，防 MSPT 尖峰。
4. **VillagerAttraction**：降频到 10 tick + 移动阈值。
5. **RecipeRuleObserver**：异步重载配方。
6. **PortalZombiePigman**：去掉 `setPersistenceRequired()`。
7. **ObserverTickControl**：强制 delay >= 1。
8. **canHasTranslations**：ConcurrentHashMap 缓存翻译。

## 修复记录（变更日志）

### 1.14.0 — 26.2 Ctrl+中键复制方块 NBT 失效

- **根因**：26.2 客户端可能不再向服务器发送 `includeData=true`，导致创造模式 Ctrl+中键无法复制方块 NBT。
- **修复**：`PickBlockNbtMixin` 用 `@Redirect` 拦截 `ServerboundPickItemFromBlockPacket#includeData()`，当 `pickBlockNbt` 规则开启时强制返回 `true`（`hasInfiniteMaterials() && true = hasInfiniteMaterials()`）。
- **新增规则**：`pickBlockNbt`（布尔，默认 `true`）。

### 1.13.7 / 1.13.6 — 26.2 Mixin 注入崩溃 + 仙人掌无效果

- **1.13.6 仙人掌无效根因**：`CactusGrowthMixin` 自定义 `canCactusSurvive` 硬编码了 `BlockTags.SAND`，而 26.2 已改用 `BlockTags.SUPPORTS_CACTUS` → 移除自定义方法，改用 `@Shadow` 声明 `canSurvive`。
- **1.13.7 崩溃修复**：`@Shadow defaultBlockState()` 无法解析（该方法继承自 `Block` 且无 refmap），改为用 `((CactusBlock)(Object)this)` 调用。

### 1.12.3 — 刷线机“刷一次就坏”

- 在 `TripwireHookBlockStringDupeMixin` 中绕过 `calculateState()` 的 `bl3 != bl5` 条件，使 fix area 每次都执行。

## 已知问题

- 无