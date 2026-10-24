# Carpet VPL Addition - 开发文档

> 本项目的代码（含全部 Mixin、规则、工具类）均由 **AI 生成**（opencode 协助编写），特此说明。

## 版本号
当前版本：v1.16.1（正式版，2026-10-24）

## ⚠️ 风险提示

本版本内容自 26.x 分支移植，可能存在 bug。

## 构建
```bash
./gradlew build
```
产物在 `build/libs/` 下：
- `carpet-vpl-addition-<version>.jar` — 发布用 jar（已 remap 到 intermediary）
- `carpet-vpl-addition-<version>-sources.jar` — 源码 jar

## 构建输出
正式发布时复制到 `D:\Ddesketp\agent\my-mods\`

## 项目结构
```
src/main/java/carpetvpladdition/
├── CarpetVPLAdditionMod.java         # Mod 入口
├── CarpetVPLAdditionExtension.java   # Carpet 扩展实现
├── mixin/                            # Mixin 类
│   ├── VersionedMixinPlugin.java     # 版本门控插件
│   ├── FastLeafDecayMixin.java       # 快速树叶腐烂
│   ├── FixedXpPerLevelMixin.java     # 固定升级经验
│   ├── FoodDataMixin.java            # 最大饱食度
│   ├── FurnaceXpControllerMixin.java # 熔炉经验倍率
│   ├── ImmatureCropProtectionMixin.java # 保护未成熟农作物
│   ├── LivingEntityMixin.java        # 最大氧气值
│   ├── NoKelpGrowthMixin.java        # 禁止海带生长
│   ├── NoSnowAccumulationMixin.java  # 禁止下雪
│   ├── ObserverTickControlMixin.java # 侦测器触发延迟
│   ├── PlayerAttributeMixin.java     # 玩家属性
│   ├── PortalZombiePigmanMixin.java  # 地狱门刷僵尸猪人
│   ├── PushableFurnaceMixin.java     # 可推动熔炉
│   ├── RecipeManagerAccessor.java    # 配方管理器访问器
│   ├── RecipeManagerMixin.java       # 不死图腾配方
│   ├── StackableItemMixin.java       # 物品堆叠修改
│   ├── SugarcaneBonemealMixin.java   # 甘蔗骨粉催熟
│   ├── TridentVoidReturnMixin.java   # 三叉戟虚空返回
│   ├── TripwireHookBlockStringDupeMixin.java # 刷线机（绕过 calculateState Mojang 修复）
│   ├── TripWireBlockStringDupeMixin.java   # 刷线机（修复 updateSource 扫描断裂）
│   ├── VillagerAttractionMixin.java  # 村民吸引
│   ├── VillagerGolemMixin.java       # 村民傀儡
│   ├── VillagerNoPriceOnAttackMixin.java # 攻击不涨价
│   └── VillagerReincarnationMixin.java # 转世村民
├── settings/
│   ├── CarpetVPLAdditionSettings.java # 规则定义 + 数值缓存
│   └── RecipeRuleObserver.java       # 配方规则验证器
└── util/
    ├── CompoundTagValueOutput.java   # NBT 输出工具
    └── SpawnEggHelper.java           # 刷怪蛋工具
```

## 跨版本兼容

通过 `VersionedMixinPlugin` 实现版本门控：

| MC 版本   | 启用 | 禁用的 Mixin |
|-----------|------|-------------|
| 1.21.0-1.21.1 | 基础功能 | VillagerGolemMixin, PortalZombiePigmanMixin, TripwireHookBlockStringDupeMixin |
| 1.21.2-1.21.5 | 大部分功能 | VillagerReincarnationMixin |
| 1.21.6-1.21.10 | 全部功能 | (无) |
| 1.21.11+ | 全部 + 配方 | RecipeManagerAccessor, RecipeManagerMixin |
| 26.x | 全部 | (无) |

## 性能优化（v1.10.2）

1. **StackableItemMixin** — 快速跳过门 + 缓存 `hopperMinecartStackSizeCached`
2. **数值缓存** — 所有字符串规则值在变更时解析一次存到 `...Cached` 字段
3. **FastLeafDecayMixin** — delay 从 0 改为 1，防 MSPT 尖峰
4. **VillagerAttractionMixin** — 降频到 10 tick + 移动阈值
5. **RecipeRuleObserver** — 异步重载配方
6. **PortalZombiePigmanMixin** — 去掉 `setPersistenceRequired()`
7. **ObserverTickControlMixin** — 强制 delay >= 1
8. **canHasTranslations** — ConcurrentHashMap 缓存

## 刷线机（stringDupe）修复记录

### 问题
Mojang 在 24w33a（1.21.2）修复了刷线漏洞，在 `TripWireHookBlock.calculateState()` 中加了两道防护：
1. **Mixin #1** — `TripwireHookBlockStringDupeMixin`：绕过 `calculateState()` 里的 `is(Blocks.TRIPWIRE) || is(Blocks.TRIPWIRE_HOOK)` 检查，使 setBlock 能对非线方块执行。

2. **Mixin #2** — `TripWireBlockStringDupeMixin`：绕过 `updateSource()` 中的连通性 break。  
   原版扫描逻辑在遇到非线方块（包括被水冲掉后的空气/水）时立即 break，导致第二次刷线时找不到线钩，`calculateState` 不被触发。  
   本 mixin 在 `stringDupe` 开启时让 `blockState.is(this)` 始终返回 true，使扫描能跨越缺口直达线钩。

### 为什么"用一次就坏"
- 第一次刷线：水流冲断所有线 → `updateSource` 扫描 → 遇到缺口 break → 找不到钩 → `calculateState` 不被触发
- `calculateState` 如果被触发（如定时 tick），`bl3 == bl5 == false`（ATTACHED 已为 false）→ fix area 被跳过
- 结果是第二次刷线后线钩不会再把线放回去

### 修复方式
两个 mixin 配合使用：
1. `TripwireHookBlockStringDupeMixin` — 在 `calculateState` 中绕过 Mojang 的类型检查
2. `TripWireBlockStringDupeMixin` — 在 `updateSource` 中绕过连通性 break，确保总是能找到线钩

## 已知问题
- 无

## 兼容性测试记录（1.11.4）

### 测试范围
对 1.21.* 系列各版本进行兼容性评估，实际构建验证了 5 个关键版本。

### 实际支持范围：1.21.6 ~ 1.21.10

| MC 版本 | 构建结果 | 说明 |
|---|---|---|
| 1.21.0 | FAIL（17 错误） | ValueOutput/ScheduledTickAccess/EntitySpawnReason/RecipeMap 不存在 |
| 1.21.4 | FAIL（11 错误） | ValueOutput 接口不存在（CompoundTagValueOutput 未被门控） |
| 1.21.6 | PASS | ValueOutput 引入，所有门控断点满足 |
| 1.21.8 | PASS | 当前构建目标 |
| 1.21.11 | FAIL（16 错误） | Mojang mappings 大重构，Villager/ZombifiedPiglin/ThrownTrident/ResourceLocation 路径变化 |

### 关键发现
1. `fabric.mod.json` 声明 `minecraft >=1.21.0` 但实际只支持 1.21.6+
2. `carpet >=1.4.177` 依赖与 1.21.0-1.21.5 矛盾（那些版本的 Carpet < 1.4.177）
3. `CompoundTagValueOutput` 是工具类不是 Mixin，无法被 `VersionedMixinPlugin` 门控
4. 1.21.11 需要大量适配工作（mappings 重构）

详细报告见 `compatibility-report-1.21.x.md`

## 修复记录（b0.15.1.1 — 同步 26.1-26.2 分支 v1.15.1）

### 补齐的 6 条规则（40 → 46 条，与 26 分支规则名集合完全一致）

`namedMinecartChunkLoader`、`minableBuddingAmethyst`、`amethystBoneMeal`、
`elytraFireworkMultiplier`、`tradedZombieVillagerPeace`、`beeNoAngerOnHarvest`。
新增 mixin 7 个（含 `ZombieVillagerAccessor`），mixins.json 注册 22 → 37 项；6 语言 121 → 139 键（键集一致已校验）。

### 移植时核对过的 API 差异（逐条查 `mc源码/1.21.8`，未照抄 26.x 代码）

- **票据系统同形**：1.21.8 的 `TicketStorage.addTicketWithRadius` 同样是 `new Ticket(type, ChunkLevel.byStatus(FULL) - radius)`，
  `FULL_CHUNK_LEVEL=33`、`ENTITY_TICKING_LEVEL=31`、`PORTAL=(300L, persist=true)`、`ENDER_PEARL=(40L, persist=false)` →
  矿车加载器"3×3 显式源点票 + 传送门票据"方案原样可用。
- **两处必改**：`AbstractMinecart` 在 `world.entity.vehicle` 包下（26.x 多一层 `minecart` 子包）；
  `ChunkPos` 是普通类，`x`/`z` 为 `public final` 字段而非 record 访问器（26.x 是 `x()`/`z()`）。
- **僵尸村民**：`ZombieVillager` 在 `world.entity.monster` 包下（26.x 为 `monster.zombie`）；
  `gossips` 字段与 `GossipContainer.getCountForType(GossipType, DoublePredicate)` 两版一致。
- **完全同形、直接搬**：`BoneMealItem.growCrop(ItemStack,Level,BlockPos)Z`、
  `BeehiveBlock.useItemOn` + `CampfireBlock.isSmokeyPos(Level,BlockPos)Z`、
  `FireworkRocketEntity.tick` 内 `look*1.5` 推进公式、`BuddingAmethystBlock.canClusterGrowAtState`、
  `AmethystClusterBlock.FACING/WATERLOGGED`、`ServerPlayerGameMode.destroyBlock(BlockPos)Z`、
  `EnchantmentHelper.getItemEnchantmentLevel(Holder<Enchantment>, ItemStack)` + `Enchantments.SILK_TOUCH`(ResourceKey)。

### 一并同步的行为修复

- **数值规则改了不即时生效**：注册 `SettingsManager.registerGlobalRuleObserver`（Carpet 1.4.177 已提供该 API，javap 确认）。
  根因同 26 分支：`ParsedRule.set` 先跑 validator、后写字段，validator 里读字段必拿旧值。
  受益规则：`maxPlayerHealth`、`playerAttackDamage`、`maxAir`、`observerTickDelay`、`furnaceXpMultiplier`、
  `cactusGrowthMultiplier`、`hopperMinecartStackSize`、`elytraFireworkMultiplier`。
- **未同步**：26 分支 b1.15.1.2 的 `stackableAnyCached` 聚合缓存优化。本分支 `StackableItemMixin` 是直接读 7 个布尔字段，
  因此也**不存在**那边的"首次开启不生效"问题；纯性能项，留待单独评估。
- **未处理**：`noZombieHorseSpawn` 在本分支仍是"有规则无 mixin"的死规则。原因见 `TODO-ZombieHorseSpawn-26.1-compat.md`：
  1.21.8 中 `SpawnPlacements` 给 `ZOMBIE_HORSE` 注册的是 `ZombieHorse::checkZombieHorseSpawnRules`，
  而该 mixin 注入 `Monster::checkMonsterSpawnRules`，补过来同样无效；文档已记"1.21.6 分支不处理"。

### 实机验证（2026-09-25）

本地用 fabric-installer 起 **1.21.8** 服务端（Carpet 1.21.7-1.4.177 + Fabric API 0.136.0+1.21.8 + 本模组 b0.15.1.1）：
`Done (1.862s)!` 正常启动，无 Mixin 报错；控制台逐条开关 6 条新规则全部被接受
（`namedMinecartChunkLoader: true` … `elytraFireworkMultiplier: 2.0`）。
**游戏内实际效果未测**（骨粉是否长水晶、矿车是否真加载区块、蜜蜂是否不追人等仍需真人验证）。

## 修复记录（v0.14.5）

### v0.14.5 — 正式版发布（由测试版 b1.14.5.1 转正，2026-09-12）

- **转正式版**：b1.14.5.1 与 26.1-26.2 分支（v1.14.5）规则**完全对齐（40 条）**，静态自查通过后转正。
- **自查记录（转正前）**：
  - 6 个新 Mixin 的 1.21.8 目标逐一核对源码：`FlowingFluid.tick(ServerLevel, BlockPos, BlockState, FluidState)` 四参、`ServerPlayer.loadAndSpawnEnderPearl`/`placeEnderPearlTicket(ServerLevel, ChunkPos)J`（static）、`AnvilBlock.damage(BlockState)`、`ServerGamePacketListenerImpl.handlePickItemFromBlock` + `ServerboundPickItemFromBlockPacket.includeData()`、`BlockEntity.setLevel(Level)`、`Level.tickBlockEntities`（protected 可注入）——全部存在且签名一致。
  - `VersionedMixinPlugin` 确认不拦截新增 Mixin。
  - `RecipeManager.prepare`/`byKey`/`Recipe.isSpecial`/`ServerRecipeBook.addRecipes(Collection, ServerPlayer)` 均 1.21.8 在位。
  - **refmap 机制确认**：本分支 intermediary 构建无 refmap 文件是正常现象——loom 在 remapJar 时直接把 Mixin 注解中的 Mojang 名重写为 intermediary 写入 class 字节码（解包验证：FlowingFluidTickMixin 含 `method_15778(class_3218;class_2338;class_2680;class_3610;)V`、ServerPlayerPearlMixin 含 `method_64123(class_3218;class_1923;)J`，与实测可用的 1.14.2 jar 同机制）。
  - 26.2 分支 API 名适配：`Identifier`→`ResourceLocation`、`ServerLevelEvents`→`ServerWorldEvents`（1.21.x Fabric API 事件名），全项目无 Identifier 残留。
- **版本号**：1.14.2 → b1.14.5.1 → v0.14.5（测试版号对齐 26.2 分支 b1.14.5.x 线；正式版 1.21.6-1.21.10 分支版本线改用 0.x 前缀，v0.14.5 对应 26.2 分支 v1.14.5 的规则内容；初误定 b1.15.1/v1.15，已更正）。2026-10-06 按用户决定弃用 0.x 前缀惯例，测试版 b0.15.1.3 更名 b1.15.1.3、与 26 分支 b1.15.1.x 补丁线对齐——当初判 b1.15.1 误定是因为彼时规则内容尚停在 26.2 分支 v1.14.5 代际，现规则已自 26 分支 v1.15.1 全量移植，b1.15.1.3 名实相符。
- jar 命名同步 26.2 分支格式：`[vpl-v0.14.5-for-1.21.6-1.21.10]carpet-addition-v0.14.5.jar`。

### b1.14.5.1 — 与 26.2 分支规则对齐（测试版）

- **删除 8 条规则**（与 26.2 分支 b1.14.3.7 同步）：stackableBucket / maxArmor / maxSaturation / stackableMusicDisc / stackableMilkBucket / stackablePowderSnowBucket / stackableEnderPearl / stackableSign。`StackableItemMixin` 删除对应 6 个物品分支（两分支该 Mixin 实现不同：1.21.x 用 @Inject，26.2 用 ItemInstance 接口，不可直接覆盖）；`FoodDataMixin` 整个删除；`CarpetVPLAdditionExtension.applyAttributes` 删除 maxArmor 块。
- **移植 10 条规则**：anvilNoDamage、calciteRecipe、tuffRecipe、woolToString、quartzUnpack（配方 4 条+铁砧）、pearlChunkKeepalive、lavaSourceOnLapisOre、pickBlockNbt、beaconUnifiedPPUpdate、noZombieHorseSpawn（规则保留但 1.21.x 无效，翻译已标注仅 26.2 生效；ZombieHorseSpawnMixin 不移植）。
- **6 语言对齐**：从 2 语言（en_us/zh_cn）补齐到 6 语言（+zh_tw/es_ar/fr_fr/pt_br），121 键与 26.2 分支完全一致（脚本校验键集）。
- 修复误提交的 TODO 文档：git rm --cached + .gitignore 补 `TODO-*.md`。
- 版本号：1.14.2 → b1.14.5.1。

## 修复记录（1.11.5）

### 修复的问题
1. **S-05**：`fabric.mod.json` 依赖声明矛盾 → 改为 `minecraft >=1.21.6 <1.21.11`，`carpet >=1.4.176`
2. **S-01**：`CompoundTagValueOutput` 实现 `ValueOutput`（1.21.6+）→ 通过限制支持范围到 1.21.6+ 解决
3. **S-04**：1.21.11 mappings 大重构（Villager→npc.villager、ZombifiedPiglin→monster.zombie、ThrownTrident→projectile.arrow、ResourceLocation→Identifier）→ 通过 `<1.21.11` 限制避免崩溃，真正适配需架构级重构（多版本构建或 intermediary mappings）
4. **M-04**：`TripWireBlockStringDupeMixin` 未加入 `VersionedMixinPlugin` 门控 → 已补上 1.21.2+ 门控
5. **M-03**：`TripwireHookBlockStringDupeMixin`/`TripWireBlockStringDupeMixin` 的 `ordinal` 偏移风险 → 加 `require = 0` 兜底

### 1.21.11 适配说明
1.21.11 改了 5 个核心类路径（见上），单一 jar 用 Mojang mappings 无法同时兼容 1.21.6-1.21.10 和 1.21.11。要支持 1.21.11 需要：
- 方案 A：多版本构建（两个 jar，每个针对一组 mappings）
- 方案 B：改用 intermediary mappings 开发（开发体验下降）
- 方案 C：用 `@Mixin(targets=字符串)` + 反射（运行时性能损失，代码复杂）

当前采取 `<1.21.11` 限制，避免玩家加载崩溃。

## 修复记录（1.14.2）

### 说明
- 多版本优化：实际构建验证 **1.21.6 / 1.21.7 / 1.21.8 / 1.21.9 / 1.21.10** 全部通过（之前 1.21.7/1.21.9/1.21.10 仅为推断）。
- **SpawnEggHelper** 兼容修复：1.21.9 起 `DataComponents.ENTITY_DATA` 类型从 `CustomData` 变为 `TypedEntityData<EntityType<?>>`，改为运行时检测 + 反射设置，保证一个 jar 通吃 1.21.6~1.21.10。
- 支持范围不变：`minecraft >=1.21.6 <1.21.11`。
- jar 命名规范：产物名 = `[vpl-<version>-for-<支持范围>]carpet-addition-<version>.jar`（如 `[vpl-1.14.2-for-1.21.6-1.21.10]carpet-addition-1.14.2.jar`）。

## 修复记录（1.14.1）
## 修复记录（1.14.1）

### 说明
- 版本号从 1.13.5 提升至 **1.14.1**，与 26.2 分支版本号保持同步，并移植 26.2 分支的功能更新：
  1. **AttributeRefreshValidator**（26.2 1.13.5）：`maxPlayerHealth` / `playerAttackDamage` / `maxArmor` 规则变更时即时应用到所有在线玩家，无需重进服务器。
  2. **CactusGrowthMixin** 改用 `@Shadow canSurvive`（26.2 1.13.6）：复用原版生存判定，替代手写 `canCactusSurvive`，避免判定逻辑漂移。
- **未包含** 26.2 分支的 Ctrl+中键 NBT 修复（`PickBlockNbtMixin` / `pickBlockNbt` 规则），1.21.8 分支不添加。
- 本分支所有代码由 **AI 生成**。

## 修复记录（1.12.3）— 刷线机“刷一次就坏”

### 根因
Mojang 在 24w33a / 1.21.2 对 `TripWireHookBlock.calculateState()` 做了两处修复，
之前 1.11.3/1.11.4/1.11.5 加的两个 mixin 只解决了**第一次**，无法持续：

1. fix area 循环类型守卫 `is(TRIPWIRE) || is(TRIPWIRE_HOOK)` —— 已由 `TripwireHookBlockStringDupeMixin`（ordinal 3）绕过。
2. fix area 外层条件 `if (bl3 != bl5)` —— 这是“刷一次就坏”的真正原因。
   - `bl3` = 线钩**旧的** ATTACHED；`bl5` = 本次扫描算出的**新的** ATTACHED。
   - 第一次：bl3=false ≠ bl5=true → 进入 fix area，被冲掉的线被重新放置（成功）。
   - 之后：线钩 ATTACHED 已为 true，bl3==bl5 → fix area 被跳过，被冲掉的线不再回放 → 断。

### 修复
在 `TripwireHookBlockStringDupeMixin` 中新增对 ATTACHED 读取的绕过（`getOptionalValue(ATTACHED)` ordinal 1），
当 `stringDupe` 开启时恒返回 `Optional.of(false)`，使 bl3 恒为 false。
于是 `bl3 != bl5` 在 bl5=true 时恒为真，fix area 每次都执行，重新放置被水冲掉的线。

三个 mixin 协同：`TripwireHookBlockStringDupeMixin`（类型守卫 + bl3）+ `TripWireBlockStringDupeMixin`（updateSource 连通性）。
