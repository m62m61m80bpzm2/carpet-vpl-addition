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

- 当前版本：**v1.14.3**（正式版）
- 规则：**测试版本号前加 b 前缀**（如 1.14.2 之后的测试版 = b1.14.3.1，用户测试通过后去掉 b 转正式版）。
- git 提交信息 = 当前版本号。

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