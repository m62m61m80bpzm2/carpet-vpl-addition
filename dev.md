# Carpet VPL Addition - 开发文档

## 版本号
当前版本：1.13.7（每次修改后 +1）

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

## 修复记录（1.13.6）— 26.2 仙人掌规则无效果

### 根因
`CactusGrowthMixin` 自定义了一个 `canCactusSurvive` 方法复制了生存检查逻辑，
但硬编码了 `BlockTags.SAND`，而 26.2 的 `CactusBlock.canSurvive` 已改用 `BlockTags.SUPPORTS_CACTUS`。
导致所有仙人掌生长判定都失败（花/长高/AGE 增量均被跳过），`cactusGrowthMultiplier` 和 `cactusBoneMeal` 看起来"无效"。

### 修复
移除自定义 `canCactusSurvive` + `asCactus()`，改用 `@Shadow` 声明 `canSurvive` 和 `defaultBlockState`，
直接调用目标类原方法，自动匹配当前 MC 版本的生存逻辑。

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
