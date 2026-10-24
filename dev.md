# Carpet VPL Addition - 开发文档

> **声明：本项目的代码（含全部 Mixin、规则、工具类）均由 AI 生成，特此说明。**

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

- 当前版本：**b1.16.1.1**（测试版，支持范围扩到 MC 26.1 ~ 26.3；最新正式版 v1.15.1）
- 规则：**测试版本号前加 b 前缀**（如 1.14.2 之后的测试版 = b1.14.3.1，用户测试通过后去掉 b 转正式版）。
- git 提交信息 = 当前版本号。

## 与 1.21.6-1.21.10 分支的同步状态

两分支规则已对齐（各 46 条）。26.1-26.3 侧最新 **v1.16.1**（正式版，含 26.3 兼容 + villagerNoWitchFromLightning，pinkSheepSpawnMultiplier 隐藏中）；1.21.x 侧最新 **b0.15.1.3**（10-06 更名 b1.15.1.3 后的版本字符串见该分支），为移植版，**可能存在 bug**。

## 技术栈与环境

| 项目 | 版本 |
|---|---|
| Minecraft | 26.1.2（非混淆版编译目标，产物兼容 26.1 ~ 26.2） |
| Java（运行/编译） | 25 |
| Fabric Loader | 0.19.3 |
| Fabric API | 0.154.2+26.1.2 |
| Carpet | 26.1+v260401 |
| fabric-loom | 1.17-SNAPSHOT（官方 ID `net.fabricmc.fabric-loom`） |
| mappings | 无（26.x 为非混淆版本，直接使用 Mojang 官方命名，无需任何 mappings） |
| Gradle | 9.5.1（wrapper） |

## 构建与发布

```bash
./gradlew build --no-daemon
```

产物位于 `build/libs/`：
- `[vpl-v1.14.3-for-26.1-26.2]carpet-addition-v1.14.3.jar` — 发布用 jar（直接以 Mojang 官方命名编译，无需 remap）
- `[vpl-v1.14.3-for-26.1-26.2]carpet-addition-v1.14.3-sources.jar` — 源码 jar

> 注意：若出现“卡住不动”的假死，通常是上次超时被杀掉的 Gradle daemon 遗留了 Loom 缓存锁。
> 用 `--no-daemon` 构建，或先执行 `./gradlew --stop` 清理。

### 发布路径

正式发布时复制到 `D:\Ddesketp\agent\my-mods\`，不要覆盖旧文件。

### Gradle 关键配置（gradle.properties / build.gradle）

```properties
minecraft_version=26.1.2
loader_version=0.19.3
fabric_version=0.154.2+26.1.2
carpet_version=26.1+v260401
mod_version=v1.14.3
mc_support_range=26.1-26.2
archives_base_name=[vpl26.2]carpet-vpl-addition
```

```groovy
// build.gradle（26.x 非混淆版要点）
plugins {
    id 'net.fabricmc.fabric-loom' version '1.17-SNAPSHOT'   // 非混淆版用官方完整 ID
    id 'maven-publish'
}
// 无需 mappings 声明、无需 modImplementation（非混淆版没有 mod* DSL，用 implementation）
sourceCompatibility = JavaVersion.VERSION_25
it.options.release = 25
```

### 依赖声明（fabric.mod.json）

```json
"depends": {
  "fabricloader": ">=0.19.3",
  "minecraft": ">=26.1 <26.3",
  "java": ">=25",
  "fabric-api": "*",
  "carpet": ">=26.1"
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
│   ├── BeaconBlockEntityTrackerMixin.java # 信标追踪（setLevel 注入）
│   └── RecipeManagerAccessor/RecipeManagerMixin.java # 不死图腾配方
├── settings/
│   ├── CarpetVPLAdditionSettings.java    # 规则定义 + 数值缓存
│   └── RecipeRuleObserver.java           # 配方规则验证器
└── util/
    ├── CompoundTagValueOutput.java   # NBT 输出工具
    ├── SpawnEggHelper.java           # 刷怪蛋工具
    └── BeaconPPUpdateManager.java    # 信标统一PP更新（20GT脉冲）
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
| 1.21.6-1.21.10 | 全部启用（由分支 1.21.6-1.21.10 维护，intermediary 编译） |
| 1.21.11 | 全部启用（由分支 1.21.6-1.21.10 维护，intermediary 编译） |
| 26.1 ~ 26.2 | 全部启用（本分支 1.21.11-26.2 维护，以 26.1.2 为编译目标的非混淆直编产物） |

> **重要**：26.x 是非混淆版本（无 intermediary 映射，日志会提示 `Mappings not present!`），必须用按 26.x 直接编译的 jar；intermediary 编译的 jar 在 26.x 上 mixin 目标全部失效（`class_XXXX` 找不到）+ Carpet API 签名不匹配（`AbstractMethodError`）。因此 1.21.11 与 26.x **无法共用一个 jar**，本分支（1.21.11-26.2）实际以 **26.1.2** 为编译目标，产物兼容 **26.1 ~ 26.2**（经源码核对，26.1/26.1.1/26.1.2/26.2 的服务端 API、Carpet 26.1/26.2 的 Validator/Extension/Rule API 签名完全一致）；1.21.6 ~ 1.21.11 由分支 1.21.6-1.21.10 维护。

> **26.3 暂不支持**：26.3 尚处 snapshot 阶段，本分支不声明 26.3 兼容（fabric.mod.json 上限 `<26.3`）。待 26.3 正式发布且实机验证后再放开。

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

### b1.16.2.1 — 修正 5 条配方规则的过时生效方式文案（测试版）

- **问题**（用户报告"显示的还是需要重新进入"，1.21.6 分支 b1.16.1.2 同修）：26 分支的 5 条配方规则（totem/calcite/tuff/woolToString/quartzUnpack）desc 与 extra.0 仍写着"需要 /reload 或重进存档生效"——实际自 b1.14.4.2 起已是开关即时生效+自动解锁配方书，文案是修复前遗留。totem 的 extra.0 更出现前半句"需要 reload"、后半句"26.3 起即时生效"的自相矛盾。
- **修正**（6 语言 × 5 条 × desc/extra.0）：统一改为"开关即时生效，并自动解锁到在线玩家的配方书，无需 /reload 或重进"；同时移除 extra.0 尾部"26.3 起经动态数据包实现"的版本限定表述（26.1/26.2 走 mixin 方案同样即时生效，该句有误导）。es_ar 的 totem 旧句形态不同（Recetas dinámicas al recargar recursos）单独处理。
- **仅翻译文案改动，代码零改动**。1.21.6 分支已在 b1.16.1.2 同修。
- 版本号：v1.16.1 → b1.16.2.1。

### v1.16.1 — 正式版发布（由测试版 b1.16.1.6 转正）

- **转正式版**：测试线 b1.16.1.1~b1.16.1.6 经用户实测通过，转正式版 v1.16.1。
- 含内容：Carpet 26.3 正式版兼容（依赖升级 + 门控修复 + 配方规则动态数据包方案）、MixinExtras 0.5.5（26.2 崩溃修复）、`villagerNoWitchFromLightning`（雷电不劈村民成女巫）；`pinkSheepSpawnMultiplier` 已隐藏暂不随版发布。
- 规则数：48 条（可见 47 + 隐藏 1）。
- 版本号：b1.16.1.6 → v1.16.1。

### b1.16.1.6 — 暂时隐藏 pinkSheepSpawnMultiplier 规则（测试版）

- 按用户要求暂时隐藏 `pinkSheepSpawnMultiplier`（粉羊生成倍率）：注释该字段的 @Rule 注解即可不注册到 Carpet——/carpet 列表不显示、set 命令返回 Unknown rule；字段与缓存逻辑保留（mixin 读到默认 1.0 = 原版行为），恢复时取消注释即可。
- **验证**：26.3 服务端 RCON 实测——list 不出现该规则、`carpet pinkSheepSpawnMultiplier 50.0` 返回 "Unknown rule"；`villagerNoWitchFromLightning` 单条查询正常可见。
- 版本号：b1.16.1.5 → b1.16.1.6。

### b1.16.1.5 — 新增 2 条规则：粉羊倍率 + 雷电不劈村民成女巫（测试版）

- **`pinkSheepSpawnMultiplier`**（数值倍率，默认 1.0，中文：粉色羊生成倍率）：调整新生成的羊中出现粉色羊的概率倍率。原版机制（26.1~26.3 同构）：`Sheep.finalizeSpawn` 按气候三档（SheepColorSpawnRules）roll 颜色，粉羊概率恒定 0.82 × 1/500 = 0.164%（约 1/610）。实现：`SheepColorSpawnMultiplierMixin` 注入 finalizeSpawn TAIL，按倍率精确概率重分配——m>1 时非粉羊以 (target−p0)/(1−p0) 翻转为粉，m<1 时粉羊以 m 概率保留否则回退白色，数学上保证最终频率 = 原版概率 × 倍率。影响所有新生成的羊（含繁殖）。
- **`villagerNoWitchFromLightning`**（布尔，默认 false，中文：雷电不劈村民成女巫）：村民被雷击不再变身女巫，只受正常雷击伤害。实现：`VillagerNoWitchFromLightningMixin` @Redirect Villager.thunderHit 里的 Mob.convertTo 调用强制返回 null——原版自身有 `if (witch == null) super.thunderHit` fallback，短路后自然走"普通雷击不变身"分支（方案 A）。
- **兼容性**：26.1.2 与 26.3 的 finalizeSpawn（4 参）与 thunderHit/convertTo（EntityType/ConversionParams/AfterConversion 擦除签名）逐一核对一致，各一个 mixin 通吃；convertTo 的 EntityType 类名在两版相同（EntityTypes 是 26.3 另一入口类，无冲突）。
- **验证**：26.3 与 26.2 双服务端 RCON 切换两条规则全部接受，零 Mixin 异常；26.3 配方解锁（unlocking）不受影响。
- 6 语言翻译同步（139→145 键，键集一致）。
- 版本号：b1.16.1.4 → b1.16.1.5（沿用 b1.16.1.x 补丁线；初误定 b1.17.1.1，已按用户要求更正并删除 my-mods 中的错误版本 jar）。

### b1.16.1.4 — 修复 26.2 客户端开服崩溃（MixinExtras 升级 0.5.5）

- **崩溃现象**（用户 26.2 单人档，错误报告 2026-10-11_14.17.48）：Bootstrap 阶段 `Mixin transformation of ServerLevel failed` → `FactoryRedirectWrapperMixinTransformer` 内 `ArrayList cannot be cast to AnnotationNode`。实测服务端同因复现，且**所有 @Redirect mixin 目标类都崩**（移除 NoSnow 后换 BeehiveBlock 原样崩）——排除特定 mixin 问题。
- **环境对照**：26.3 服务端（loader 0.19.5 内置 MixinExtras 0.5.5）同代码正常；26.2（loader 0.19.3 内置 MixinExtras 0.5.4）必崩。而我们 jar bundle 的是 **MixinExtras 0.4.1**——0.4.1 与 0.5.4 运行时交互触发 FactoryRedirectWrapper 的 CCE bug，0.5.5 已修复。
- **修复**：build.gradle 的 `include(annotationProcessor(mixinextras-fabric))` **0.4.1 → 0.5.5**。jar-in-jar 版本高于 loader 内置版本时 loader 采用高版本，26.2 环境从此统一跑 0.5.5。
- **验证**：26.2 服务端（fabric-api 0.155.2+26.2 + carpet 26.2+v260616）`Done (2.394s)` 零异常（b1.16.1.4 同 jar）；26.3 服务端回归 `unlocking 1 recipes` 正常。
- 版本号：b1.16.1.3 → b1.16.1.4。
- **分支改名**：26.1-26.2 → **26.1-26.3**（本地 + origin + private 三处，GitHub rename API，支持范围已扩至 26.3 名实一致）。

### b1.16.1.2 — Carpet 26.3 正式版兼容 + 配方规则 26.3 数据包方案（测试版）

- **背景**：用户报告 Carpet 依赖从项目起一直是 `26.1+v260401`。查证：Carpet 官方已有 26.2 正式版（26.2+v260616）与 **26.3 正式版（26.3+v261008，maven）**，b1.16.1.1 的 26.3 兼容当时卡在 Carpet 只有 beta-3。
- **依赖升级**：carpet_version 26.1+v260401 → **26.3+v261008**（玩家侧 9 月发布的 26.3+v260915 与 v261008 同为 26.3 正式线，API 一致）。以 MC 26.1.2 + Carpet 26.3 编译通过，fabric.mod.json 的 minecraft 依赖 <26.4（b1.16.1.1 已扩）。
- **修复 1（b1.16.1.1 遗留漏洞）：VersionedMixinPlugin 的 isAtLeast26_3 声明后从未赋值**，26.3 门控永不生效 → RecipeManagerMixin 在 26.3 上找不到 prepare 注入点开服崩（本次 26.3 实机首启即复现）。修复：26.x 分支解析 minor>=3 赋值 + catch 块同步重置。
- **修复 2（用户报告"26.3 羊毛分解无效"）**：26.3 配方加载彻底重构——RecipeManager 从 Registries.RECIPE 可重载注册表构建（RegistryDataLoader 用 Recipe.DIRECT_CODEC 解析 datapack JSON），prepare 注入点永久消失。5 条配方规则改用**动态数据包方案**（新类 RecipeDataPackManager）：
  - 规则开启 → 配方 JSON 写入 `<world>/datapacks/carpet-vpl-addition/data/carpet-vpl-addition/recipe/<id>.json`，由原版 FolderRepositorySource 发现、RegistryDataLoader 注册；关闭 → 删文件。
  - **关键细节 ①**：26.3 世界数据包的 PackRepository id 带 `file/` 前缀（PackLocationInfo("file/"+目录名)），addPack/查询必须用 `file/carpet-vpl-addition`；data 下命名空间目录仍是 `carpet-vpl-addition`（无前缀）。
  - **关键细节 ②**：pack.mcmeta 用 26.3 权威格式（照抄原版内置包）：**不写 pack_format**，只有 description+min_format+max_format（=97）。带 pack_format:97 会触发 validatePackFormatForRange 额外校验导致 pack 被 FolderRepositorySource 静默跳过（available 无此包）。
  - **关键细节 ③**：pendingReload 竞态——Carpet 配置加载（observer 触发 reloadAndUnlock）早于 onServerLoadedWorlds（pack 才被 add），第二次调用被 pendingReload 吞掉致配方永不加载。改为 queuedReload 排队，当前 reload 完成后补跑。
  - 统一入口 RecipeRuleObserver.reloadAndUnlock(server)（updatePackFiles + reloadResources + whenComplete unlock），observer 与 onServerLoadedWorlds 共用。
- **验证**：26.3 专用服务端 + RCON 端到端：启动即 `registry size=2043（2042+1）, unlocking 1 recipes: [carpet-vpl-addition:string_from_wool]`；规则 false→文件删除+removePack、true→写回+addPack，开关即时生效。6 语言 5 条配方规则 extra.0 的"26.3 暂不生效"过时说明更新为"26.3 起经动态数据包实现，开关即时生效"（139 键一致）。
- 版本号：b1.16.1.1 → b1.16.1.2。

### v1.15.1

### b1.16.1.1 — 扩展支持 Minecraft 26.3（测试版）

- 目标：`26.1-26.2` 分支的产物同时覆盖 **26.1 ~ 26.3**。仍按项目既有做法**以最低支持版本 26.1.2 编译**（`minecraft_version` 不动），靠"引用面在三个版本都存在"来保证向上兼容。
- 配置改动：
  - `gradle.properties`：`mod_version` v1.15.1 → b1.16.1.1；`mc_support_range` 26.1-26.2 → **26.1-26.3**；`archives_base_name` 同步。
  - `fabric.mod.json`：`minecraft` 依赖 `>=26.1 <26.3` → **`>=26.1 <26.4`**（不改这条，26.3 上加载器会直接判为不兼容）；顺带把 `description` 从旧文案（只提配方/铁砧/信标/珍珠）改成 46 条规则的实际概览。
- **26.3 兼容性核对方法**（先把 26.3 源码反编译到 `D:\Ddesketpgent\mc源码.3\`，7302 个 .java）：
  1. 从已发布 jar 的常量池提取全部 `net/minecraft` 引用：**91 个类、221 处成员引用**。
  2. 类存在性：91/91 在 26.3 的 client.jar + server jar 中都在；26.2 有而 26.3 删除的 129 个原版类，**我们一个都没引用**。
  3. 成员回归：逐类对比 26.2 / 26.3 的声明集合，**没有任何"26.2 声明、26.3 消失"的成员**；56 处两版都不在本类声明的，是继承/接口方法，两版情况相同。
  4. 带描述符的 8 个 `@At` 注入目标（`ScheduledTickAccess/ServerLevel#scheduleTick`、`Biome#shouldSnow`、`ServerPlayer#placeEnderPearlTicket`、`ServerboundPickItemFromBlockPacket#includeData`、`BlockState#is/getOptionalValue`）在 26.2 与 26.3 **同形**。
  5. 关键机制文件 `TicketType` / `TicketStorage` / `ChunkLevel` 与 `FireworkRocketEntity.tick` 推进公式：26.3 与 26.2 **逐字节/逐行相同** → 矿车区块加载器与鞘翅倍率不受影响。
- 26.3 侧观察到的原版变化（未波及本项目）：`Block`/`BuddingAmethystBlock` 去掉了 `MapCodec`/`codec()`；`EntityType`→`EntityTypes` 的改名延续到 26.3（26.1.2 仍是旧名，故编译目标留在 26.1.2 时不能新写引用 `EntityTypes` 的代码）。
- 前置现状：Carpet 官方 maven 最新只到 **`26.3-beta-3+v260820`**，尚无 26.3 正式版；本模组 `depends.carpet` 仍是 `>=26.1`，26.3 上要装 Carpet 的 26.3-beta 构建。
- 待办：**26.3 实机开服验证**（静态核对不能覆盖"调用点 owner 变化导致 @At 失配"这类问题）；`noZombieHorseSpawn` 的"仅 26.2 生效"限定语与源码不符（26.1.2/26.3 同样注册 `Monster::checkMonsterSpawnRules`），待实机确认后修 6 语言文案。
- **用 26.3 映射试编译（`-Pminecraft_version=26.3 -Pfabric_version=0.161.0+26.3 -Pcarpet_version=26.3-beta-3+v260820`）暴露出两个静态核对看不到的真问题**，已修：
  1. **`BonemealableBlock` 三个方法在 26.3 全部加了 `BonemealSource` 参数**（`isValidBonemealTarget`/`isBonemealSuccess`/`performBonemeal` 描述符都变）。旧 `CactusBonemealMixin` 是 `@Mixin(CactusBlock.class) implements BonemealableBlock` + `@Override`，在 26.3 上旧签名不再覆盖任何接口方法 → 规则静默失效（且 `@Override` 使按 26.3 编译直接失败）。
     **改法**：`CactusBonemealMixin` 重写为注入 `BoneMealItem.growCrop(ItemStack, Level, BlockPos)Z` 的 HEAD——该描述符 26.1/26.2/26.3 三版一致，且玩家手点与发射器共用此路径，故一个 jar 覆盖三版、不需要版本门控。节数与高度判定照旧（上方空且整根不足 3 节才长）。
  2. **26.3 的 `RecipeManager` 删掉了 `prepare(ResourceManager, ProfilerFiller)`，且不再有 `registries` 字段**（配方改由注册表装载，`RecipeMap.create(RegistryLookup)`）。我们的 `RecipeManagerMixin`（`@ModifyReturnValue` on `prepare`）与 `RecipeManagerAccessor`（`@Accessor("registries")`）在 26.3 上**找不到注入点 → 开服即 Mixin 崩溃**，不是"规则没效果"那么轻。
     **改法**：`VersionedMixinPlugin` 新增 `isAtLeast26_3` 判定（解析版本号第二段，>26 主版本亦视为满足），在 26.3+ 直接不注册这两个 mixin；`RecipeRuleObserver.onValueChange` 同条件提前返回，避免调用已不存在的原版 API。5 条配方规则（`totemRecipe`/`calciteRecipe`/`tuffRecipe`/`woolToString`/`quartzUnpack`）在 26.3 上因此无效，已在 6 语言的 `extra.0` 追加"版本限定"说明（139 键不变、键集一致）。
  2b. **同一接口的第二处受害者 `SugarcaneBonemealMixin`**：它用 `@Implements(@Interface(iface = BonemealableBlock.class, prefix = "cane$"))`，
     26.3 试编译**不报错**（方法名没加 @Override，javac 不管），但真起 26.3 服务端时 Mixin 在应用阶段抛
     `isValidBonemealTarget does not exist in target interface BonemealableBlock` → **开服直接失败**。
     同样改为注入 `BoneMealItem.growCrop`，保留"对柱内任一格都按整根柱算高度、满 3 节不再长"的原行为。
     教训：**只靠静态核对 + 试编译不足以覆盖 Mixin 的接口校验，必须真起服务端跑一遍。**
  3. 修完后 26.3 试编译只剩 `RecipeManagerMixin` 的 2 处源码级类型错误（`Recipe.CODEC` 返回 `Holder<Recipe<?>>`、`RecipeMap.create` 改收 `HolderLookup`）——该类在 26.3 已被插件关闭、运行期不参与，属预期残留。**结论：本 jar 必须以 26.1.2 编译**（`minecraft_version` 不要改成 26.3）。
- 版本号：v1.15.1 → b1.16.1.1。

### v1.15.1 — 正式版发布（由 b1.15.1.6 转正，第二次）

- 用户实测 b1.15.1.6 通过（鞘翅推进不再抖动、骨粉对母岩生效、数值规则改完即时生效、监守者规则已移除）后转正。
- 本 v1.15.1 与先前那次被撤回的同名版本**内容不同**：本次含 b1.15.1.5 + b1.15.1.6 两轮修复。
  被撤回的旧 jar（内容等同 b1.15.1.4）已从 `my-mods\测试\` 清掉，其原件仍留在项目 `build/libs/` 可回溯；
  新 jar 放入 `my-mods\正式.1-26.2\`，v1.14.5 等旧正式版未覆盖。
- 本版累计内容：`namedMinecartChunkLoader` 重做（3×3 显式源点票 + 传送门票据 15 秒且随存档持久化 + 6 语言命名匹配）、
  新增 `beeNoAngerOnHarvest`、数值规则缓存时序根因修复（Carpet validator 早于字段赋值 → 改用全局规则观察者）、
  `amethystBoneMeal` 补母岩路径、`elytraFireworkMultiplier` 改为缩放目标速度、删除 `noWardenSpawn`，
  以及 b1.15.1.1~b1.15.1.3 的 5 条新规则、性能优化与开服崩溃修复。
- 规模：规则 **46 条**，6 语言各 **139 键**，mixin 注册 **38 项**。
- 产物校验（解包 jar 实测）：`fabric.mod.json` 的 `version` = `v1.15.1`；mixin 注册 38 项、无 `NoWardenSpawnMixin` 残留（class 也不在包内）；
  6 语言各 139 键且鞘翅文案已是"巡航速度倍率"新版。
- **未推送公共仓库、未发 Release**（等用户明确要求）。

### 实机验证结果（2026-09-25，本地起真实 Fabric 服务端，非静态推断）

| MC 版本 | 前置 | 结果 |
|---|---|---|
| **26.1.2** | Carpet 26.1+v260401 + Fabric API 26.1.2 | `Done (1.081s)!` 正常启动；控制台 `carpet cactusBoneMeal true`、`bedrockSugarcaneBonemeal true`、`amethystBoneMeal true` 全部接受 |
| **26.2** | Carpet 26.2+v260616 + Fabric API 0.161.0+26.2 | `Done (0.767s)!` 正常启动；五条规则（含两条重写、一条数值 `elytraFireworkMultiplier 2.0`）实时开关全部生效，无 Mixin 报错 |
| **26.3** | Carpet **26.3-beta-3+v260820** + Fabric API 0.161.0+26.3 | 我方 38 个 Mixin **全部应用通过**（sugarcane 的接口错误已消失）；启动最终失败在 Carpet 自身：`carpet.script.CarpetScriptServer.parseFunctionClasses` 抛 `NoClassDefFoundError: DensityFunction$FunctionContext`，即 **Carpet 的 26.3-beta 跟不上 26.3 正式版**，与本模组无关，需等 Carpet 出 26.3 正式版 |

复现方式：`fabric-installer 1.1.2` 装 server，`mods/` 放 fabric-api + carpet + 本模组 jar，`eula=true`，
用 `(sleep 95; echo "carpet <规则> true"; sleep 8; echo "stop") | java -Xmx1536M -jar fabric-server-launch.jar nogui` 驱动控制台。

### b1.15.1.6 — 修正鞘翅烟花倍率的缩放对象（测试版）

- **现象**：用户实测 b1.15.1.5 后报告"鞘翅有加速效果，但推进一抽一抽的"。
- **根因**：原版推进公式为 `movement.add(look*0.1 + (look*1.5 - movement)*0.5)`，
  `1.5` 是**推进目标速度**、`0.5` 是**朝目标的平滑逼近系数**、`0.1` 是一次性冲量。
  b1.15.1.1 起的实现是 @Redirect 那个 `Vec3.add(DDD)`、把**整个增量**乘以倍率，
  等价于把平滑逼近系数也放大：递推变为 `v' = (1-0.5m)v + 0.85m·look`，
  m=2 时一步顶满、之后靠滑行衰减回落，且稳定速度上限并没有提高 → 手感忽快忽慢。
- **修复**：改为 `@ModifyConstant(method="tick", constant=@Constant(doubleValue=1.5))`，
  只缩放目标速度：`v' = 0.5v + (0.1+0.75m)·look`，逼近节奏仍由原版 0.5 控制，
  稳定速度 `(0.2+1.5m)·look` 随倍率线性提升 → 加速平滑。
  tick() 内 `1.5` 共 4 处：鞘翅分支 x/y/z 三处（生效）+ 一处赋给从未使用的局部变量 `power`（无害）。
  `FireworkRocketEntity#tick` 26.1.2 与 26.2 该方法体除 lambda 写法外完全一致，常量分布相同。
- 编译期踩坑记一下：`@Constant` 需要 `import org.spongepowered.asm.mixin.injection.Constant`，
  漏掉会报"找不到符号 类 Constant"（本次首次构建即因此失败）。
- 6 语言 `elytraFireworkMultiplier` 的 name/desc/extra 同步改写为"巡航速度倍率 + 不再抖动"；
  README 对应行同步。键数仍 139（6 语言一致）。
- 版本号：b1.15.1.5 → b1.15.1.6。
- **待实测**：设 2.0 后飞行应是"越飞越快并稳定在更高速度"，而不是顶一下掉一下；
  设 5.0 这类大倍率也不该再出现抖动或失控。

### b1.15.1.5 — 数值规则缓存时序 bug + 母岩骨粉 + 删除监守者规则（测试版）

- **【根因确认，影响面大】改规则不生效**：用户报告 `elytraFireworkMultiplier` 设了没反应。javap 反编译 Carpet 26.1（`carpet.settings.ParsedRule` 私有 `set`）确认写入顺序为
  **① 遍历 validators → ② `Field.set` 写静态字段 → ③ `notifyRuleChanged`**。
  而 `CacheRefreshValidator.validate` 内调用的 `syncNumericCaches()` 读的是**静态字段本身**，于是 validator 拿到的一律是旧值：游戏内改一条数值规则，对应缓存停留在上一次的值为 `1.0`/默认，表现为"完全无效"；再改别的规则或重启后才恢复。
  启动时 `onGameStarted` 里那次 `syncNumericCaches()` 是在配置全部写入之后跑的，所以**只有游戏中动态修改**会中招。
  受害规则：`maxPlayerHealth`、`playerAttackDamage`、`maxAir`、`observerTickDelay`、`furnaceXpMultiplier`、`cactusGrowthMultiplier`、`hopperMinecartStackSize`、`elytraFireworkMultiplier`，以及 `stackableAnyCached` 聚合开关（7 条 `stackable*` 首次开启会不生效）。
- **修复**：`CarpetVPLAdditionExtension.onGameStarted` 注册 `SettingsManager.registerGlobalRuleObserver(...)` → 回调在 `notifyRuleChanged`（第③步）中触发，此时字段已写入新值，刷出来的缓存一定正确。`registerGlobalRuleObserver` 为 public static、`RuleObserver.ruleChanged(CommandSourceStack, CarpetRule<?>, String)`，26.1 已 javap 确认存在。
  保留原 validator 不动（其刷新会被观察者覆盖，属冗余但无害），避免再碰 validator 泛型这个已经崩过一次的雷区。
- **`amethystBoneMeal` 补母岩路径**（用户报告"没效果"）：原实现只处理点到**芽**的情况；实际玩家准星对着母岩那一面时 `growCrop` 收到的 pos 是 `BUDDING_AMETHYST` 本体，母岩不是 `BonemealableBlock`，`growCrop` 直接返回 false，故整体看起来毫无作用。现补上：对母岩使用骨粉时按原版同一套朝向判定依次尝试 6 个面，能长就长（空面出新芽、同朝向的芽推进一档），六面全被占住则**不消耗骨粉**。复用原版公开成员 `BuddingAmethystBlock.canClusterGrowAtState` 与 `AmethystClusterBlock.FACING/WATERLOGGED`，只去掉原版随机刻的 1/5 概率门槛。`BuddingAmethystBlock` 源码 26.1.2 与 26.2 完全一致。
- **删除 `noWardenSpawn`**（用户要求）：`NoWardenSpawnMixin.java`、`@Rule` 字段、mixins.json 注册项、6 语言 3 键、README 表格行全部清除。规则 47 → **46 条**，mixins 注册 39 → **38 项**，6 语言 142 → **139 键**（键集一致性脚本校验通过）。
- 版本号：v1.15.1 → b1.15.1.5（撤回转正，回到测试线）。

### （已撤回）v1.15.1 第一次转正 —— 由 b1.15.1.4 转正，未推公共仓库、未发 Release，用户实测发现两条规则无效后撤回

- 用户游戏内验证通过后转正，无额外代码改动（仅 `gradle.properties` 的 `mod_version` / `archives_base_name` 与 README、dev.md 版本行）。
- 本版累计内容：`namedMinecartChunkLoader` 重做（3×3 源点票 + 传送门票据 15 秒且随存档持久化 + 6 语言命名匹配）、新增 `beeNoAngerOnHarvest`，以及 b1.15.1.1~b1.15.1.3 的 6 条新规则、性能优化与开服崩溃修复。
- 规则 47 条，6 语言各 142 键。
- 产物校验（解包 jar 实测，非推断）：`fabric.mod.json` 的 `version` = `v1.15.1`；`carpet-vpl-addition.mixins.json` 共 39 项（`BeeNoAngerOnHarvestMixin`、`NamedMinecartChunkLoaderMixin` 均已注册）；两个 mixin 的 class 与 6 个 lang 文件都已进包。
- 放置：`my-mods\正式\26.1-26.2\[vpl-v1.15.1-for-26.1-26.2]carpet-addition-v1.15.1.jar`（v1.14.5 等旧版保留未覆盖）。

### b1.15.1.4 — 矿车区块加载器重做 + 新增采蜜不惊蜂（测试版）

- **`namedMinecartChunkLoader` 修复「矿车一跨区块边界就永久冻结」**（功能缺陷，非崩溃）。
- **26.x 票据等级机制**（javap 核实 26.1.2 与 26.2 的 `TicketStorage` / `ChunkLevel` / `ServerChunkCache` / `DistanceManager`）：
  - `ServerChunkCache.addTicketWithRadius(type, pos, r)` → `new Ticket(type, ChunkLevel.FULL_CHUNK_LEVEL(33) - r)`；
  - **等级数值越低权限越高**：`isEntityTicking(l) = l <= 31`、`isBlockTicking(l) = l <= 32`、`fullStatus(l <= 33) = FULL`；
    `FullChunkStatus` 是包含关系，等级 31 的区块同样跑方块实体（漏斗/熔炉/作物）；
  - 票据源点向邻格扩散时每格权限降 1 等（`ChunkTracker extends DynamicGraphMinFixedPoint`），33 即视为未被覆盖；
  - `TicketType` 在 26.x 已是 registry record `(long timeout, int flags)`，flags 位
    `PERSIST=1 / LOADING=2 / SIMULATION=4 / KEEP_DIMENSION_ACTIVE=8 / CAN_EXPIRE_IF_UNLOADED=16`；
    `Ticket` 的泛型 value 载荷已被删除，去重只看 (type, level)，重复贴同一张票仅 `resetTicksLeft()`。
- **根因**：旧实现只在矿车所在区块贴一张 radius 2（等级 31）票 → 只有锚点格跑实体，邻格被扩散为 32
  （只跑方块、**不跑实体**）。矿车跨出锚点格即停止 tick → 本 Mixin 不再执行 → 无人续期 →
  票据过期后整片卸载，永久卡在区块边界且无法自愈。与 `pearlChunkKeepalive` 同属"实体停 tick 即断续期"死锁族。
- **修复**：显式给以矿车为中心的 3×3 区块各贴一张源点票（不依赖扩散推断），仍每 20gt 续期一次；
  9 次 map 调用/秒/加载器，同 type+level 由原版去重，无累积。
- **票据类型 `ENDER_PEARL` → `PORTAL`**：timeout 300gt（15 秒）对比 40gt，且 flags=15 含 `FLAG_PERSIST`。
  26.x `TicketStorage` 本身是 `SavedDataType`，但 `packTickets` 只序列化 `persist()` 为真的票据，
  故传送门票据随世界存档保存、重启后恢复；珍珠票不入库，重启即全丢。
  已遍历 26.1.2 全部 10682 个类：仅 15 个类引用 `TicketType`，无任何代码移除 PORTAL 票据，借用不会被传送门逻辑误清。
  额外收益：PORTAL 带 `FLAG_KEEP_DIMENSION_ACTIVE`，所在维度不休眠——跨维度（末地/下界）农场才会真的运行。
- **名称匹配扩展**：归一化（转小写、剔除空白与下划线）后匹配 6 语言等价名
  （`chunkloader` / `区块加载器` / `區塊載入器` / `cargadordechunks` / `chargeurdechunks` / `carregadordechunks`），
  并对含 `chunkloader` 子串的名字放行，便于玩家加前后缀区分多台加载器。
- **新增规则 `beeNoAngerOnHarvest`（采蜜不惊蜂）**：`@Redirect` `BeehiveBlock.useItemOn` 内的
  `CampfireBlock.isSmokeyPos(Level, BlockPos)`，规则开启即视为"已被营火熏"。原版随即只走 `resetHoneyLevel` 分支，
  一次注入同时避开两条仇恨路径：`angerNearbyBees`（巢外蜂 `setTarget`）与
  `emptyAllLivingFromHive` 的 EMERGENCY 释放（巢内蜂 `setTarget`）。
  仅覆盖取蜜：`playerDestroy`（工具砸巢）与 `onExplosionHit` 不经过该判定，仇恨照旧。
  `isSmokeyPos` 签名 26.1.2/26.2 一致（`CampfireBlock` 仅 `BlockEntityType`→`BlockEntityTypes` 改名差异）。
- **6 语言同步至 142 键**（新增 bee 3 键 + 改写 cart 3 键，键集一致性脚本校验通过）。
- 版本号：b1.15.1.3 → b1.15.1.4（用户决定沿用 b1.15.1.x 补丁线，本次不新开 1.15.2 档）。
- **实测项（已由用户游戏内验证通过，随后转正 v1.15.1）**：矿车跨区块边界续期正常；范围内方块实体运行；
  重启服务器后票据随存档恢复；取蜜不再追人而砸毁蜂巢仍追人。

### b1.15.1.3 — 修复开服崩溃（测试版）

- **崩溃**：用户报告开服即崩，`ClassCastException: Boolean cannot be cast to String`，位于 `CacheRefreshValidator.validate`，调用链 Carpet `attachServer → loadConfigurationFromConf → resetToDefault → ParsedRule.set → validator`。
- **根因**：b1.15.1.2 给 7 条 `stackable*` **布尔规则**挂了 `CacheRefreshValidator extends Validator<String>`——Carpet 的 validator 泛型必须与规则值类型匹配，布尔规则在 resetToDefault 时把 Boolean 强转 String 崩溃。开服必崩。
- **修复**：新增 `CacheRefreshValidatorBoolean extends Validator<Boolean>`（validate 内同样调用 syncNumericCaches 刷新聚合缓存），7 条 stackable 规则改挂 Boolean 版。
- **复查**：脚本全量校验所有规则挂载的 validator 泛型与字段类型一一匹配（CacheRefreshValidator/AttributeRefreshValidator→String，CacheRefreshValidatorBoolean/RecipeRuleObserver→boolean），无其他错配。
- 版本号：b1.15.1.2 → b1.15.1.3。

### b1.15.1.2 — 性能优化（测试版）

- **StackableItemMixin 热路径聚合缓存**：`getMaxStackSize` 是物品堆叠计算的最热路径（渲染/物流/合成高频调用），原每次调用执行 `anyStackableEnabled()`（7 个布尔字段读取+OR）。改为单缓存布尔 `stackableAnyCached`，由 `syncNumericCaches()` 统一刷新；为此给 7 条 stackable 布尔规则挂上 `CacheRefreshValidator`（此前它们无 validator，改规则不会刷新缓存）。
- **TradedZombieVillagerMixin 短路顺序**：`checkDespawn` 对所有 Mob 每 tick 触发，将难度判断（枚举比较）提到 instanceof 之前，非和平服务器直接短路，避免对全量 Mob 做 instanceof。
- **NamedMinecartChunkLoaderMixin 节流顺序**：`tickCount % 20` 提前到维度判断之前，规则开启时每矿车 19/20 的调用在最便宜的判断处直接返回。
- 审查结论（无需改动）：FurnaceXpController/ObserverTickControl/NoSnowAccumulation/NoKelpGrowth/CactusGrowth 等均已使用缓存字段且快速路径在最前；FlowingFluidTickMixin 对非岩浆流体仅 2 次布尔判断即返回；BeaconPPUpdateManager 规则关闭时第一条即返回；RecipeRuleObserver 反射仅在 reload（低频）时执行。
- 版本号：b1.15.1.1 → b1.15.1.2。

### v1.14.5

### v1.14.5 — 正式版发布（由测试版 b1.14.5.2 转正）

- **转正式版**：测试版 b1.14.5.1 / b1.14.5.2 经测试通过，转正式版 v1.14.5。
- 含内容：新增规则 `lavaSourceOnLapisOre`（青金石矿石岩浆源，无限水机制）。
- README 同步：版本号、安装 jar 名；补齐 v1.14.4 时遗漏的 `pearlChunkKeepalive` 规则行（规则表 40 条）。
- 版本号：b1.14.5.2 → v1.14.5。

### b1.14.5.2 — 规则改名 + 底座改青金石矿石 + 版本号更正（测试版）

- **规则改名**：`lavaSourceOnLapis` → `lavaSourceOnLapisOre`（原名不能准确表达底座是矿石），Mixin 方法名同步。
- **底座变更**：青金石块 → **青金石矿石**，深层青金石矿石同样生效（`LAPIS_ORE` / `DEEPSLATE_LAPIS_ORE`）。
- **编译修复**：26.2 的 `BlockState.is()` 无可变参数重载（与 1.21.x 不同），两个矿石判断改为两条件相或。
- **版本号更正**：以上功能性变更应升 b1.14.5.2，而非沿用 b1.14.5.1（前两次提交误用 b1.14.5.1 号）。
- 6 语言键名（carpet.rule.lavaSourceOnLapisOre.*）与文案同步更新（121 键一致）。
- 版本号：b1.14.5.1 → b1.14.5.2。

### b1.14.5.1 — 新增青金石岩浆源规则（测试版）

- **新增规则 `lavaSourceOnLapis`**（默认关闭，中文名：青金石岩浆源）：复刻无限水机制——流动岩浆的水平相邻 4 个方向中至少 2 个是岩浆源（水源不算），且正下方是青金石块时，在下一次流体计划 tick 转化为岩浆源方块；省去反复倾倒岩浆桶。
  - **实现**：`FlowingFluidTickMixin` 注入 `FlowingFluid.tick` HEAD（26.2 所有流动流体计划 tick 的统一入口，签名 `(ServerLevel, BlockPos, BlockState, FluidState)`）。满足条件时 `setBlock` 为岩浆源并 `ci.cancel()` 取消本次 spread；源方块放置后由 `LiquidBlock.onPlace` 重新排程 tick，之后完全按原版源岩浆行为流动。
  - **细节**：主世界岩浆流动慢，转换最多延迟约 1.5 秒（下界更快）；仅流动岩浆参与转换，岩浆源不干预；转换后关闭规则不影响已生成的源。
  - **迭代**：初版条件"下方是青金石块即转"被用户纠正为无限水机制（需相邻 2 个岩浆源），同版本内修正。
- 6 语言翻译同步（各 121 键，脚本校验键集一致）。
- 版本号：v1.14.4 → b1.14.5.1。

### v1.14.4 — 正式版发布（由测试版 b1.14.4.2 转正）

- **转正式版**：测试版 b1.14.4.1 / b1.14.4.2 经测试通过，去掉 b 前缀转正式版 v1.14.4。
- 含内容：5 条日常生存规则（anvilNoDamage / calciteRecipe / tuffRecipe / woolToString / quartzUnpack）、pearlChunkKeepalive（修复重进后珍珠区块偶发不加载）、配方规则即时生效+自动解锁配方书、6 语言全支持（es_ar/fr_fr/pt_br/zh_tw）、noZombieHorseSpawn 标注仅 26.2 生效。
- 版本号：b1.14.4.2 → v1.14.4。

### b1.14.4.2 — 新增珍珠区块保活规则 + 6 语言全支持（测试版）

- **新增规则 `pearlChunkKeepalive`**（默认关闭，中文名：修复珍珠区块加载）：修复原版 1.21.5+ 珍珠 ticket 机制的"续期断档死锁"（用户实机报告：退出重进后珍珠区块偶发不加载，珍珠悬空冻结无法传送，手投珍珠复现）。
  - **根因**（26.2 源码确认）：玩家退出重进时 `loadAndSpawnEnderPearl` 只放置一次 ENDER_PEARL ticket（半径 2 区块，超时 40gt），之后完全依赖"区块加载成功 → 珍珠 tick → 续期"链条；chunk 系统调度偶发竞态断掉链条 → 区块卸载 → 珍珠永不 tick → 永不续期 → 永久死锁。
  - **修复（最小干预）**：`ServerPlayerPearlMixin` 注入 `loadAndSpawnEnderPearl` 的 `placeEnderPearlTicket` 调用之后（MixinExtras `@Local` 捕获 pearl 局部变量），同步强制加载珍珠所在区块（`getChunk(FULL, true)`，仅一次，1 个区块开销可忽略）。区块确定就位后珍珠立即 tick，由原版机制自行续期接管，无需持续干预。初版的"每 20gt 持续续期"方案被否定（过度设计），已简化。
  - **文案**：六语言介绍均侧重"修复的 bug"本身（珍珠区块不加载/珍珠冻结），不绑定具体使用场景。
- **6 语言全支持**：新增 `es_ar`（西语）、`fr_fr`（法语）、`pt_br`（葡语-巴西）、`zh_tw`（繁体，zhconv 词级转换 + 台服术语：終界珍珠/地獄/生怪蛋），与现有 `en_us`/`zh_cn` 覆盖 Carpet language 规则的全部选项；6 文件键集完全一致（各 117 键，脚本校验）。
- **配方规则即时生效 + 自动解锁配方书**：修复开启配方规则后需 /reload 才生效、且配方书不显示的问题。
  - **根因**：b1.14.4.1 的 4 条新配方规则（calciteRecipe/tuffRecipe/woolToString/quartzUnpack）未挂 `RecipeRuleObserver` validator，开启时不触发资源重载，RecipeMap 不更新。
  - **修复**：4 条规则补挂 validator；`RecipeRuleObserver` 增强为"重载完成后自动解锁"——reload 完成（原版已向玩家广播配方包，合成台即刻可用）后，将当前开启的动态配方通过 `ServerRecipeBook.addRecipes()` 解锁到所有在线玩家配方书（解锁+新配方高亮+客户端立即显示，触发 RECIPE_UNLOCKED 准据），无需 /reload 或重新登录。
  - 实现细节：`RecipeRuleObserver` 内置规则字段→配方 ID 映射（与 RecipeManagerMixin.DYNAMIC_RECIPES 对应），重载完成后反射读取规则开关、`RecipeManager.byKey` 查找 holder，仅解锁已开启规则的配方。
- 版本号：b1.14.4.1 → b1.14.4.2。

### b1.14.4.1 — 新增 5 条日常生存规则 + noZombieHorseSpawn 标注 + 珍珠区块调查（测试版）

- **新增规则 `anvilNoDamage`**（默认关闭）：铁砧使用不损坏。`AnvilNoDamageMixin` 注入 `AnvilBlock.damage` HEAD，规则开启时返回原 BlockState（完整/缺口/损坏铁砧均不降级不消失，不影响坠落损坏）。
- **新增规则 `calciteRecipe`**（默认关闭）：方解石合成——安山岩+骨粉 → 2 方解石（无序）。材料全部可再生（安山岩=闪长岩+圆石，骨粉=骷髅/堆肥）。
- **新增规则 `tuffRecipe`**（默认关闭）：凝灰岩合成——圆石+骨粉 → 2 凝灰岩（无序）。初版方案"深板岩圆石+骨粉"被否（深板岩不可再生），改为圆石（刷石机无限）。
- **新增规则 `woolToString`**（默认关闭）：羊毛分解——任意颜色羊毛 1 → 线 4（`#minecraft:wool` 标签 17 色通吃）。与原版 4 线→1 羊毛对称可逆。
- **新增规则 `quartzUnpack`**（默认关闭）：石英块分解——石英块 1 → 下界石英 4。与原版 4:1 对称可逆；仅支持普通石英块（柱/雕纹/砖为 1:1，分解会凭空溢出）。
- **RecipeManagerMixin 重构**：单 totem 配方改为 `DynamicRecipe` 列表结构（规则字段名 + 配方 ID + JSON），新增配方只需加一条记录；单条解析失败不影响其余配方。
- **noZombieHorseSpawn 兼容性标注**（TODO 文档结案）：不采用门控方案，翻译 desc/extra 直接标注"【仅在 MC 26.2 及以上版本生效，26.1/26.1.2 上无效】"；1.21.6 分支不处理。
- **末影珍珠区块不加载调查**（用户游玩报告，26.2 偶发：退出重进后珍珠所在区块有时不加载）：
  - 机制溯源（1.21.5+ 原版新增，26.2 保留）：飞行中的珍珠每 40gt 通过 `ServerPlayer.registerAndUpdateEnderPearlTicket` 给所在区块加 `TicketType.ENDER_PEARL`（半径 2 区块）ticket；玩家退出时珍珠序列化进玩家数据（`saveEnderPearls`，ender_pearls 列表）；重进时 `loadAndSpawnEnderPearl` 重新放置珍珠并加 ticket。
  - 已定位的三个原版机制缺口（均非本 Mod 引入，本 Mod 无任何珍珠/区块相关 mixin）：
    1. `loadAndSpawnEnderPearl` 中维度未就绪或实体加载失败时**静默跳过**（日志 "Failed to spawn player ender pearl..." / "Trying to load ender pearl without level..."）→ 珍珠丢失，区块自然无 ticket；
    2. ticket 续期依赖珍珠 tick 且 owner 必须解析为在线玩家；owner=null 的珍珠（发射器射出）不进玩家数据也不注册 ticket，区块卸载即冻结且不自愈（不加载→不 tick→不续期死锁）；
    3. ticket 超时 40gt，若退出/重进时序异常导致续期断档，区块卸载后珍珠冻结。
  - 待复现确认：检查服务器日志是否出现上述两条 warn；确认珍珠是手投还是发射器射出。
- 版本号：v1.14.3 → b1.14.4.1。

### v1.14.3 — 正式版发布（由测试版 b1.14.3.8 转正）

- **转正式版**：测试版 b1.14.3.8（含 b1.14.3.1 ~ b1.14.3.8 全部修复）经测试通过，去掉 b 前缀转正式版 v1.14.3。
- 版本号：b1.14.3.8 → v1.14.3。

### b1.14.3.8 — 修复 noZombieHorseSpawn 无效（改拦 Monster.checkMonsterSpawnRules）（测试版）

- **规则无效的根因**：旧实现注入 `ZombieHorse.finalizeSpawn` 返回 null，但 NaturalSpawner 只是把返回值赋给 `groupData`、**从不检查是否为 null**，实体照样 `addFreshEntityWithPassengers` 生成。
- **修复**：改为拦截 `Monster.checkMonsterSpawnRules`（僵尸马在 SpawnPlacements 注册的生成前置判断 `Monster::checkMonsterSpawnRules`）。NaturalSpawner.isValidSpawnPostitionForType 在生成前调用 `SpawnPlacements.checkSpawnRules(type, level, NATURAL, pos, random)`，此时对僵尸马直接返回 false，根本不会进入生成流程。
- **类型比较**：用 EntityTypeHelper（BuiltInRegistries 按 ID 查）跨版本比较 `zombie_horse`，避免 26.1/26.2 常量位置差异。
- 版本号：b1.14.3.7 → b1.14.3.8。

### b1.14.3.7 — 删除 8 个规则（末影珍珠/告示牌/奶桶/细雪桶/唱片/空桶堆叠 + 最大护甲值/最大饱食度）（测试版）

- **删除堆叠规则**：`stackableEnderPearl`（末影珍珠）、`stackableSign`（告示牌）、`stackableMilkBucket`（奶桶）、`stackablePowderSnowBucket`（细雪桶）、`stackableMusicDisc`（唱片）、`stackableBucket`（空桶）。
- **删除属性规则**：`maxArmor`（最大护甲值）、`maxSaturation`（最大饱食度）。
- **同步清理**：Settings 中对应规则字段与缓存（maxArmorCached / maxSaturationCached）及其解析；StackableItemMixin 中对应分支与 anyStackableEnabled；CarpetVPLAdditionExtension.applyAttributes 中 maxArmor 应用；删除整个 FoodDataMixin 并移除 mixins.json 注册；中英文翻译、README 同步删除。
- 版本号：b1.14.3.6 → b1.14.3.7。

### b1.14.3.6 — 信标PP更新改用PP更新(updateNeighbourShapes)+TE阶段最前面统一触发（测试版）

- **信标 PP 更新仍无效的根因（重要）**：26.2 的侦测器（ObserverBlock）**不重写 neighborChanged（NC 更新入口）**，只在 `updateShape`（PP 更新入口）中响应：`if (FACING == directionToNeighbour && !POWERED) startSignal(...)`。因此上一版对信标位置调 `updateNeighborsAt`（NC 更新）侦测器无反应。
- **修复**：改为对信标位置调用 `BlockState.updateNeighbourShapes(level, pos, UPDATE_NEIGHBORS)`（PP 更新 / shape update，等价于“信标被重新放置”时向 6 个邻居发出的形状更新），侦测器通过 updateShape 正确响应。
- **触发阶段改为 TE 阶段最前面**：新增 `BeaconTickPhaseMixin` 注入 `Level.tickBlockEntities` 的 HEAD（方块实体阶段最前面，遍历 tick 任何 BE 之前）。同一阶段内按 TreeSet 坐标顺序连续更新全部信标，100 个信标的更新顺序固定、连续执行，中间不插入其他方块实体的 tick；各维度在其自身 TE 阶段最前面更新（与信标自身“信标线程”BeaconBlockEntity.tick 检测下方方块处于同一阶段）。
- **移除**：`ServerTickEvents.END_SERVER_TICK` 钩子与全部控制台打印。
- 版本号：b1.14.3.5 → b1.14.3.6。

### b1.14.3.5 — 修复信标PP更新无效 / 移除控制台打印 / disableSnow不再禁冰 / 甘蔗骨粉只能长2格（测试版）

- **信标 PP 更新无效**：原先对信标**正下方青金石原矿**调 `updateNeighborsAt`，信标收到的是“邻居变化”通知，但 `BeaconBlock` 不响应邻居更新 → 无可观测效果。改为对**信标位置**调 `updateNeighborsAt(pos, BEACON)`（等价于“信标被重新放置”），信标四周（含上方红石线、下方原矿）全部收到方块更新并重新计算。
- **移除控制台打印**：删除 `BeaconPPUpdateManager.onServerTick` 中的 `System.out.println`（不再每次触发刷屏）。
- **disableSnow 不再禁止结冰**：原先 HEAD cancel 整个 `tickPrecipitation`（积雪+结冰一起禁）。改为 `@Redirect` 仅拦截 `Biome.shouldSnow`（积雪层生成判断），水面结冰（`shouldFreeze`）保持原版行为。
- **甘蔗骨粉只能长 2 格**：`SugarcaneBonemealMixin` 原先要求 `pos.above()` 为空气 → 对底部那格第二次用骨粉时上方已是甘蔗，永远 false。重写为：从 pos 向上找顶端、顶端上方是空气且总高 < 3 才可催熟；对柱内任意一格用骨粉都能长到原版上限 3 格。
- 版本号：b1.14.3.4 → b1.14.3.5。

### b1.14.3.4 — 修复规则注册崩溃（翻译 key 格式错误导致全部规则消失）（测试版）

- **严重 Bug（规则全部消失的根因）**：`stringDupe` 和 `villagerNoPriceOnAttack` 两条老规则在语言文件中的翻译 key 使用了旧前缀 `carpet-vpl-addition.rule.<name>`，而 Carpet 26.2 要求 `carpet.rule.<name>.name / .desc / .extra.N` 格式。`parseSettingsClass` 在注册时找不到 `carpet.rule.stringDupe.desc` 直接抛异常（`No language key provided for carpet.rule.stringDupe.desc`），**导致整个规则类注册中断——游戏里 `/carpet list` 看不到任何 VPL 规则（包括 b1.14.3.1 的信标统一PP更新与 b1.14.3.2 的禁止僵尸马生成）**，设置规则时报“未知的规则选项”。
- **修复**：语言文件（zh_cn / en_us）中这两条规则的 key 全部改为标准 `carpet.rule.<name>.name/.desc/.extra.0` 格式，并补上缺失的 `.name` key。
- **验证**：脚本逐一核对 41 条规则在两种语言中都有 `.name` 与 `.desc`，无旧前缀残留、无孤儿 key。
- 版本号：b1.14.3.3 → b1.14.3.4。

### b1.14.3.3 — 编译目标降至 26.1.2，产物兼容 26.1 ~ 26.2（测试版）

- **编译目标 26.2 → 26.1.2**：26.1 系列（26.1 / 26.1.1 / 26.1.2）与 26.2 的服务端 API 经源码逐类核对完全一致（ZombieHorse.finalizeSpawn、SpawnPlacements 注册、BlockEntity.setLevel、ItemInstance、EntitySpawnReason、RecipeManager/RecipeMap 等），Carpet 26.1/26.2 的 Validator / CarpetExtension / Rule 注解签名一致（javap 对比）。以最低版本 26.1.2 编译，产物在 26.1 ~ 26.2 全部可用。
- **依赖更新**：fabric-api `0.154.2+26.1.2`、carpet `26.1+v260401`。
- **撤回 26.3 兼容声明**：fabric.mod.json `minecraft >=26.2 <26.4` → `>=26.1 <26.3`，carpet `>=26.2` → `>=26.1`，描述改为 "26.1 ~ 26.2"，mc_support_range `26.2-26.3` → `26.1-26.2`。26.3 尚处 snapshot，不声明支持。
- 版本号：b1.14.3.2 → b1.14.3.3。

### b1.14.3.2 — 新增禁止僵尸马生成规则（测试版）

- **新增规则 `noZombieHorseSpawn`**（默认关闭）：阻止僵尸马在夜间作为怪物自然生成。
- **实现**：`ZombieHorseSpawnMixin` 注入 `ZombieHorse.finalizeSpawn` HEAD，当规则开启且 `spawnReason == EntitySpawnReason.NATURAL` 时返回 null 拦截生成。
- **只拦截自然生成**：刷怪蛋 / 指令 / 繁殖等其他生成路径不受影响。
- **备注**：26.2 已移除“马被雷劈变僵尸马”机制（Horse 无 thunderHit 重写），僵尸马仅剩自然生成 + 刷怪蛋/指令/繁殖来源。
- 版本号：b1.14.3.1 → b1.14.3.2。

### b1.14.3.1 — 新增信标统一 PP 更新规则 + 修正 26.2 构建（测试版）

- **新增规则 `beaconUnifiedPPUpdate`**（默认关闭）：每隔 20 游戏刻，向所有正下方为浅层青金石原矿（minecraft:lapis_ore，非深板岩）的信标统一发出一次方块更新。
- **统一时机**：触发阶段为服务器 tick 末尾（ServerTickEvents.END_SERVER_TICK，所有维度 tick 完成后），所有信标在同一时刻、同一阶段被更新，与所在位置无关。
- **微时序**：追踪集合按坐标排序（TreeSet），每次触发的处理顺序固定，微时序一致。
- **阶段上报**：每次触发在服务器控制台输出 `阶段=服务器tick末尾(END_SERVER_TICK), tick=<t>, 统一更新信标数=<n>`。
- **追踪机制**：BeaconBlockEntityTrackerMixin 注入 BlockEntity.setLevel（覆盖玩家放置 / /setblock / 区块加载）+ 区块实体加载/卸载事件；触发时自愈清理失效位置。
- **修正 26.2 构建（重要）**：26.x 是非混淆版本（无 intermediary），必须直接按 26.2 编译：
  1. 插件改用官方 ID `net.fabricmc.fabric-loom` 1.17-SNAPSHOT（短 ID 无 1.17.18 marker）；无需 mappings 声明、无需 `noIntermediateMappings()`。
  2. `modImplementation` → `implementation`（非混淆版没有 mod* DSL）；无 remapJar，jar 任务直接用 `jar`。
  3. `ServerWorldEvents` → `ServerLevelEvents`（26.x 改名）；恢复被误删的 mixins.json `plugin` 字段（VersionedMixinPlugin）。
  4. 验证：jar 内无 `class_` 名、Validator 签名与 Carpet 26.2 完全一致（修复 26.2 上的 `AbstractMethodError` 崩溃）。
- **文档对齐**：README / dev.md 版本号、依赖表、jar 命名全部对齐真实构建配置（26.2 编译目标、Java 25、Carpet 26.2+v260616）。
- **清理**：删除 gradle.properties 死变量 carpet_dep_version；新增 MIT LICENSE（build.gradle 的 jar 任务引用了该文件）。
- **修复 StackableItemMixin 在 26.2 静默失效（重要）**：1.14.2 为兼容 1.21.11 改用 `@Inject ItemStack.getMaxStackSize + require=0`，但 26.2 中该方法是 `ItemInstance` 接口的 default 方法（读 `DataComponents.MAX_STACK_SIZE`），ItemStack 不重写它 → 注入点不存在 → require=0 静默跳过，**全部堆叠规则失效**。已恢复 1.13.4 验证过的 `implements ItemInstance + @Override getMaxStackSize` 方案。
- **修复 syncNumericCaches 无下限校验**：maxAir/maxSaturation/maxPlayerHealth/hopperMinecartStackSize 等接受 0 或负数，已加 `Math.max` 下限保护（hopperMinecartStackSize 保持 99 上限）。
- **向上兼容 26.3（已撤回）**：曾根据 tis-addition 的 `mapping-26.2-26.3.txt` 推断 26.2→26.3 服务端 API 零变化并放宽到 `>=26.2 <26.4`。**b1.14.3.3 已撤回该声明**：26.3 尚处 snapshot、未实机验证，现声明上限 `<26.3`。
- **全面核对 mixin 注入目标**：逐类验证全部 27 个 mixin 的 @Mixin 目标与注入方法在 26.2 源码中存在且签名匹配（含 Player.getXpNeededForNextLevel、ServerPlayer.restoreFrom、Villager.onReputationEventFrom、RecipeManager.prepare 等）。


### 1.14.2 — 错误的“多版本”尝试（已由 b1.14.3.1 修正）

> **教训记录**：此版本曾试图以 1.21.11 为编译目标、产物同时支持 1.21.11 ~ 26.2，但经 26.2 实机测试被推翻——26.x 无 intermediary，intermediary 编译的 jar 在 26.2 上 mixin 目标全部失效 + Carpet API 签名不匹配（`AbstractMethodError`）。1.14.2 的 jar 不可用于 26.2。

- **目标变更**：26.2 分支从“仅 26.2”改为同时支持 **1.21.11 ~ 26.2**（fabric.mod.json 声明 `minecraft >=1.21.11 <26.3`）。
- **构建目标**：编译目标从 26.2 改为 **1.21.11**。
- **修复 build.gradle 关键问题**：
  1. 插件 ID `net.fabricmc.fabric-loom` → `fabric-loom`。
  2. `implementation` → `modImplementation`，补回 `mappings loom.officialMojangMappings()`。
  3. Java 25 → Java 21（1.21.11 运行要求）。
- **API 适配（1.21.11 尚无 26.2 新 API）**：
  - `EntityTypes` → `EntityType`（PortalZombiePigmanMixin / VillagerGolemMixin / SpawnEggHelper）。
  - `StackableItemMixin` 从 `implements ItemInstance`（26.2 专属）改回 `@Inject getMaxStackSize`（兼容 1.21.11），加 `require = 0` 兜底。
- **依赖**：carpet `>=1.4.194`，fabricloader `>=0.17.3`，java `>=21`。
- jar 命名规范：产物名 = `[vpl-<version>-for-<支持范围>]carpet-addition-<version>.jar`。

### 1.14.1 — 默认启用 pickBlockNbt

- `pickBlockNbt` 规则默认改为 `true`，装上即自动生效，无需手动开启。

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