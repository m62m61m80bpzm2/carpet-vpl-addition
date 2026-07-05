# Carpet VPL Addition — 1.21.* 系列兼容性测试报告

**测试日期**：2026-07-05
**被测版本**：carpet-vpl-addition 1.11.3
**测试方法**：静态代码审计 + 5 个关键 MC 版本实际构建验证
**构建环境**：Fabric Loom 1.17.11、Gradle 9.5.1、Java 25

---

## 一、执行摘要

### 支持矩阵总览

| MC 版本 | 构建结果 | 支持程度 | 严重 bug 数 |
|---|---|---|---|
| 1.21.0 | FAIL（17 错误） | 不支持 | 4 |
| 1.21.1 | 推断 FAIL | 不支持 | 4 |
| 1.21.2 | 推断 FAIL | 不支持 | 3 |
| 1.21.3 | 推断 FAIL | 不支持 | 3 |
| 1.21.4 | FAIL（11 错误） | 不支持 | 1 |
| 1.21.5 | 推断 FAIL | 不支持 | 1 |
| 1.21.6 | **PASS** | **完全支持** | 0 |
| 1.21.7 | 推断 PASS | 完全支持 | 0 |
| 1.21.8 | **PASS**（基线） | **完全支持** | 0 |
| 1.21.9 | 推断 PASS | 完全支持 | 0 |
| 1.21.10 | 推断 PASS | 完全支持 | 0 |
| 1.21.11 | FAIL（16 错误） | 不支持 | 1 |

### 核心结论

- **实际可支持范围**：1.21.6 ~ 1.21.10
- **fabric.mod.json 声明范围**（`minecraft >=1.21.0`）与实际不符，存在严重虚假声明
- **`carpet >=1.4.177` 依赖矛盾**：1.21.0-1.21.5 对应的 Carpet 版本号都 < 1.4.177，会导致 Fabric Loader 拒绝加载
- **1.21.11 完全不兼容**：Mojang 在 1.21.11 进行了大规模 mappings 重构，多个核心类（`Villager`、`ZombifiedPiglin`、`ThrownTrident`、`ResourceLocation`）的包路径或类名发生变化

---

## 二、按版本详细结果

### 1.21.0（Carpet 1.21-1.4.147+v240613，fabric-api 0.102.0+1.21）

**构建结果**：FAIL — 17 个编译错误

**根因**：`VersionedMixinPlugin` 是运行时门控，无法阻止编译期解析。所有引用了 1.21.2+ 才存在的类的代码都会编译失败。

**失败点清单**：
1. [CompoundTagValueOutput.java:12](file:///D:/Ddesketp/agent/carpet/src/main/java/carpetvpladdition/util/CompoundTagValueOutput.java#L12) — `ValueOutput` 接口（1.21.6+ 才存在）
2. [CompoundTagValueOutput.java:139,146,153](file:///D:/Ddesketp/agent/carpet/src/main/java/carpetvpladdition/util/CompoundTagValueOutput.java#L139) — `ProblemReporter.Problem` 内部接口
3. [FastLeafDecayMixin.java:6](file:///D:/Ddesketp/agent/carpet/src/main/java/carpetvpladdition/mixin/FastLeafDecayMixin.java#L6) — `ScheduledTickAccess`（1.21.2+）
4. [PortalZombiePigmanMixin.java:7](file:///D:/Ddesketp/agent/carpet/src/main/java/carpetvpladdition/mixin/PortalZombiePigmanMixin.java#L7) — `EntitySpawnReason`（1.21.2+）
5. [VillagerGolemMixin.java:6](file:///D:/Ddesketp/agent/carpet/src/main/java/carpetvpladdition/mixin/VillagerGolemMixin.java#L6) — `EntitySpawnReason`（1.21.2+）
6. [RecipeManagerMixin.java:17](file:///D:/Ddesketp/agent/carpet/src/main/java/carpetvpladdition/mixin/RecipeManagerMixin.java#L17) — `RecipeMap`（1.21.2+）

### 1.21.4（Carpet 1.21.4-1.4.161+v241203，fabric-api 0.110.5+1.21.4）

**构建结果**：FAIL — 11 个编译错误

**根因**：`CompoundTagValueOutput` 实现了 `ValueOutput` 接口，该接口在 1.21.6+ 才引入。虽然 `VillagerReincarnationMixin`（唯一使用该工具类的 Mixin）被 `VersionedMixinPlugin` 门控到 1.21.6+，但工具类本身**不是 Mixin**，不在门控范围内，编译器仍需解析它。

**失败点清单**：
1. [CompoundTagValueOutput.java](file:///D:/Ddesketp/agent/carpet/src/main/java/carpetvpladdition/util/CompoundTagValueOutput.java) — 全部 11 个错误均来自此类对 `ValueOutput`/`ProblemReporter.Problem` 的引用

**历史印证**：`build-error-1.21.4.txt` 记录了此前同样的失败（当时还有 `VillagerAttractionMixin.profession()` 错误，现已改用其他方式）。

### 1.21.6（Carpet 1.21.6-1.4.176+v250617，fabric-api 0.128.0+1.21.6）

**构建结果**：**PASS**（仅有 `CompoundTagValueOutput` 的 deprecation 警告）

**说明**：1.21.6 是 `ValueOutput` 接口的引入版本，`VersionedMixinPlugin` 的所有门控断点（1.21.2/1.21.6）均满足，所有 Mixin 可正常编译。

### 1.21.8（Carpet 1.21.7-1.4.177+v250630，fabric-api 0.136.0+1.21.8）

**构建结果**：**PASS**（基线对照，仅有 deprecation 警告）

**说明**：当前正式发布版本的构建目标，Carpet 1.21.7 jar 兼容 1.21.8。

### 1.21.11（Carpet 1.21.11-1.4.194+v260107，fabric-api 0.140.0+1.21.11）

**构建结果**：FAIL — 16 个编译错误

**根因**：Mojang 在 1.21.11 进行了大规模 mappings 重构（为 26.1 unobfuscation 做准备），多个核心类的包路径或类名发生变化。

**失败点清单**：
1. `net.minecraft.world.entity.npc.Villager` 找不到 — 影响 5 个文件（VillagerAttractionMixin、VillagerGolemMixin、VillagerNoPriceOnAttackMixin、VillagerReincarnationMixin、SpawnEggHelper）
2. `net.minecraft.world.entity.monster.ZombifiedPiglin` 找不到 — PortalZombiePigmanMixin
3. `net.minecraft.world.entity.projectile.ThrownTrident` 找不到 — TridentVoidReturnMixin
4. `net.minecraft.resources.ResourceLocation` 找不到 — RecipeManagerMixin

**注**：这些类在 1.21.11 中可能被移动到不同包或改名。需要查阅 1.21.11 的官方 mappings 确认新位置。

---

## 三、问题清单

### 严重 bug（导致功能完全失效）

| 编号 | 问题 | 影响版本 | 证据 |
|---|---|---|---|
| S-01 | `CompoundTagValueOutput` 实现 `ValueOutput` 接口，但该接口 1.21.6+ 才存在。工具类不被 `VersionedMixinPlugin` 门控，导致 1.21.0-1.21.5 编译失败 | 1.21.0-1.21.5 | 1.21.0 构建 17 错误、1.21.4 构建 11 错误 |
| S-02 | `RecipeManagerMixin` 引用 `RecipeMap`（1.21.2+ 才存在），未被门控 | 1.21.0-1.21.1 | 1.21.0 构建日志 |
| S-03 | `FastLeafDecayMixin` 引用 `ScheduledTickAccess`（1.21.2+），未被门控 | 1.21.0-1.21.1 | 1.21.0 构建日志 |
| S-04 | 1.21.11 多个核心类路径变化（`Villager`/`ZombifiedPiglin`/`ThrownTrident`/`ResourceLocation`），完全无法编译 | 1.21.11 | 1.21.11 构建日志 16 错误 |
| S-05 | `fabric.mod.json` 声明 `minecraft >=1.21.0` 但实际只支持 1.21.6+，玩家在 1.21.0-1.21.5 加载会被 Fabric Loader 拒绝（因 `carpet >=1.4.177` 依赖矛盾） | 1.21.0-1.21.5 | [fabric.mod.json:20-24](file:///D:/Ddesketp/agent/carpet/src/main/resources/fabric.mod.json#L20-L24) |

### 轻微瑕疵（不影响基本使用）

| 编号 | 问题 | 影响版本 | 说明 |
|---|---|---|---|
| M-01 | `CompoundTagValueOutput` 使用了过时的 API（deprecation 警告） | 1.21.6-1.21.10 | 构建成功但有警告，功能正常 |
| M-02 | `RecipeRuleObserver.reloadResources(Collection<String>)` 在 1.21.5+ 可能签名不匹配 | 1.21.5+ | 仅在玩家修改 `totemRecipeRule` 时触发，未构建验证 |
| M-03 | `TripwireHookBlockStringDupeMixin.ordinal = 3` 在 1.21.5+ `calculateState` 重构后可能偏移 | 1.21.5+ | 编译通过，但运行时可能包装到错误的 `is()` 调用 |
| M-04 | `TripWireBlockStringDupeMixin` 未加入 `VersionedMixinPlugin` 的 1.21.2+ 门控清单 | 1.21.0-1.21.1 | 与 Hook 版本配套逻辑不一致 |

### 已知限制（无法通过构建测试验证）

| 编号 | 问题 | 说明 |
|---|---|---|
| L-01 | 运行时 `AbstractMethodError` 风险 | `CompoundTagValueOutput` 实现的 `ValueOutput` 接口若在 1.21.7-1.21.10 间增删抽象方法，运行时会崩溃 |
| L-02 | Mixin `ordinal` 偏移 | `TripwireHookBlockStringDupeMixin` 的 `ordinal = 3` 假设 `calculateState` 中第 4 个 `BlockState.is(Block)` 调用是修复点，1.21.5+ 重构后可能失效 |
| L-03 | `require = 0` 的 Mixin 静默失败 | `NoSnowAccumulationMixin`、`NoKelpGrowthMixin`、`ObserverTickControlMixin`（第二个 `@ModifyArg`）加了 `require = 0`，目标缺失时不崩但 Mixin 静默不生效 |

---

## 四、修复建议（仅供参考，本次未实施）

### 优先级 P0（严重，必须修复）

1. **修正 `fabric.mod.json` 依赖声明**：将 `minecraft >=1.21.0` 改为 `minecraft >=1.21.6`，或将 `carpet >=1.4.177` 降级到 `>=1.4.176`（1.21.6 对应版本）。
2. **门控 `CompoundTagValueOutput`**：由于工具类无法用 `VersionedMixinPlugin` 门控，建议：
   - 方案 A：将 `VillagerReincarnationMixin` 的逻辑内联，不依赖独立工具类
   - 方案 B：用反射调用 `ValueOutput` 方法，避免编译期依赖
   - 方案 C：把 `CompoundTagValueOutput` 改为 Mixin（加 `@Mixin(ValueOutput.class)` 或类似），纳入门控
3. **支持 1.21.11**：查阅 1.21.11 官方 mappings，更新 `Villager`/`ZombifiedPiglin`/`ThrownTrident`/`ResourceLocation` 的 import 路径，并用 `VersionedMixinPlugin` 做双版本兼容。

### 优先级 P1（轻微瑕疵）

4. **补门控**：将 `FastLeafDecayMixin`、`ObserverTickControlMixin`、`TripWireBlockStringDupeMixin` 加入 1.21.2+ 门控清单。
5. **`TripwireHookBlockStringDupeMixin.ordinal`**：改用 `slice` + `target` 描述符匹配，避免 `ordinal` 偏移。
6. **`RecipeRuleObserver.reloadResources`**：用反射检查参数类型，兼容 1.21.5+ 的 `Collection<ResourceLocation>` 签名。

---

## 五、测试方法与证据

### 实际构建的 5 个版本

| 版本 | gradle.properties 配置 | 构建日志 | 结果 |
|---|---|---|---|
| 1.21.0 | MC=1.21, Carpet=1.21-1.4.147+v240613, FA=0.102.0+1.21 | [build-test-1.21.0.txt](file:///D:/Ddesketp/agent/carpet/build-test-1.21.0.txt) | FAIL 17 错误 |
| 1.21.4 | MC=1.21.4, Carpet=1.21.4-1.4.161+v241203, FA=0.110.5+1.21.4 | [build-test-1.21.4.txt](file:///D:/Ddesketp/agent/carpet/build-test-1.21.4.txt) | FAIL 11 错误 |
| 1.21.6 | MC=1.21.6, Carpet=1.21.6-1.4.176+v250617, FA=0.128.0+1.21.6 | [build-test-1.21.6.txt](file:///D:/Ddesketp/agent/carpet/build-test-1.21.6.txt) | PASS |
| 1.21.8 | MC=1.21.8, Carpet=1.21.7-1.4.177+v250630, FA=0.136.0+1.21.8 | [build-test-1.21.8.txt](file:///D:/Ddesketp/agent/carpet/build-test-1.21.8.txt) | PASS（基线） |
| 1.21.11 | MC=1.21.11, Carpet=1.21.11-1.4.194+v260107, FA=0.140.0+1.21.11 | [build-test-1.21.11.txt](file:///D:/Ddesketp/agent/carpet/build-test-1.21.11.txt) | FAIL 16 错误 |

### 推断的版本（基于相邻版本 + 代码审计）

- **1.21.1**：与 1.21.0 同期，Carpet 1.4.147 同时支持 1.21 和 1.21.1，预期同样 17 错误
- **1.21.2/1.21.3**：`ScheduledTickAccess`/`EntitySpawnReason`/`RecipeMap` 已引入，但 `ValueOutput` 仍不存在，预期与 1.21.4 相同的 11 错误
- **1.21.5**：`ValueOutput` 仍未引入（1.21.6 才有），预期与 1.21.4 相同的 11 错误
- **1.21.7/1.21.9/1.21.10**：与 1.21.6/1.21.8 同期，`ValueOutput` 已稳定，预期 PASS

### 测试限制

- 构建测试只能验证**编译期**和 **Mixin 应用阶段**错误，无法验证**运行时**行为（如 `ordinal` 偏移、`AbstractMethodError`）
- 中间版本（1.21.1/1.21.2/1.21.3/1.21.5/1.21.7/1.21.9/1.21.10）未实际构建，用静态分析 + 相邻版本结果推断
- `gradle.properties` 已恢复原值，与测试前完全一致

---

## 六、结论

`carpet-vpl-addition` 1.11.3 在 1.21.* 系列的**实际支持范围仅为 1.21.6 ~ 1.21.10**，与 `fabric.mod.json` 声明的 `minecraft >=1.21.0` 严重不符。主要根因是 `CompoundTagValueOutput` 工具类不被 `VersionedMixinPlugin` 门控，以及 1.21.11 的 Mojang mappings 大重构未适配。

建议开发者：
1. 立即修正 `fabric.mod.json` 的依赖声明，避免玩家在 1.21.0-1.21.5 上加载失败
2. 评估是否需要支持 1.21.11（需大量适配工作）
3. 若仅支持 1.21.6-1.21.10，则当前代码已基本就绪
