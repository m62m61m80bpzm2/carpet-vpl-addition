# Carpet VPL Addition — 1.21.* 系列兼容性测试计划

## Context（背景）

`carpet-vpl-addition` 是一个基于 Fabric + Carpet 的 Minecraft 模组，`fabric.mod.json` 声明支持 `minecraft >=1.21.0`。当前构建目标为 MC 1.21.8（Carpet 1.4.177、Java 25、Fabric Loader 0.17.3），通过 `VersionedMixinPlugin` 做版本门控（断点：1.21.2 / 1.21.6 / 1.21.11 / 26.x）。

用户要求对 1.21.* 系列各版本（1.21.0 及后续所有 1.21.x）做全面兼容性评估，区分轻微瑕疵与严重 bug（功能完全失效/数据丢失/安全隐患），并产出兼容性测试报告。

**约束**：仅测试+报告，不修改源代码；Java 25 依赖保留；构建测试时仅临时修改 `gradle.properties` 切换目标版本，测试完成后恢复。

---

## 已完成的调研结论

### Carpet 各 MC 版本对应发布版本（来自 CurseForge 调研）

| MC 版本 | Carpet 版本 | 备注 |
|---|---|---|
| 1.21.0 / 1.21.1 | 1.4.147+v240613 | 同一 jar 支持 1.21 和 1.21.1 |
| 1.21.2 / 1.21.3 | 1.4.158 | Carpet TIS Addition 矩阵确认 |
| 1.21.4 | 1.4.161 | |
| 1.21.5 | 1.4.170 | Carpet Extra 1.4.170 印证 |
| 1.21.6 | 1.4.176 | Carpet Extra 1.4.176 印证 |
| 1.21.7 | 1.4.177+v250630 | 当前 gradle.properties 用的版本 |
| 1.21.8 | ~1.4.180 | 介于 1.4.177 与 1.4.186 之间 |
| 1.21.9 / 1.21.10 | 1.4.186+v251009 | 同一 jar |
| 1.21.11 | 1.4.194+v251223 | |

### 静态代码审计已识别的高风险点（共 12 处）

1. **`VillagerAttractionMixin.profession()`**（未门控，build-error-1.21.4.txt 已印证 1.21.0-1.21.5 编译失败）
2. **`FastLeafDecayMixin`**（未门控，引用 `ScheduledTickAccess`，1.21.0-1.21.1 必崩）
3. **`ObserverTickControlMixin` 第一个 `@ModifyArg`**（未门控，同上）
4. **`TripWireBlockStringDupeMixin`**（未门控，按 VersionedMixinPlugin 注释逻辑应配 1.21.2+ 门控但漏加）
5. **`FoodDataMixin.Mth.clamp(III)I`**（1.21.5+ 可能被 `Math.clamp` 取代）
6. **`PushableFurnaceMixin.isPushable` 签名**（跨 1.21.x 多变）
7. **`LivingEntityMixin.getMaxAirSupply`**（1.21.5+ 才在 `Entity` 层）
8. **`TridentVoidReturnMixin.getMinY()`**（1.21.5+ 才有）
9. **`CompoundTagValueOutput`**（实现 `ValueOutput` 接口，1.21.7-1.21.11 间若有方法增删会运行时 `AbstractMethodError`）
10. **`RecipeRuleObserver.reloadResources(Collection<String>)`**（1.21.5+ 改为 `Collection<ResourceLocation>`）
11. **`TripwireHookBlockStringDupeMixin.ordinal = 3`**（1.21.5+ `calculateState` 重构可能偏移）
12. **`fabric.mod.json` 依赖矛盾**（`minecraft >=1.21.0` 与 `carpet >=1.4.177` 矛盾，1.21.0-1.21.6 上 Carpet 版本号都 < 1.4.177）

---

## 测试方案

### 阶段 A：静态分析报告（已完成）

基于代码审计、`VersionedMixinPlugin` 门控逻辑、历史 `build-error-*.txt` 日志、Carpet 各版本 API 知识，输出每条 Mixin 在 1.21.0-1.21.11 各版本下的风险评级（高/中/低）和具体失败模式（编译期/运行时）。

### 阶段 B：关键版本实际构建验证（5 个版本）

针对以下代表性版本，临时修改 `gradle.properties` 的 `minecraft_version`/`fabric_version`/`carpet_version`，运行 `./gradlew clean build`，捕获构建输出。

| 版本 | 选择理由 | 预期失败点（基于静态分析） |
|---|---|---|
| 1.21.0 | 早期边界，Carpet 1.4.147 | `VillagerAttractionMixin.profession()`、`FastLeafDecayMixin.ScheduledTickAccess`、`ObserverTickControlMixin`、`PushableFurnaceMixin.isPushable`、`TridentVoidReturnMixin.getMinY()` |
| 1.21.4 | build-error-1.21.4.txt 印证失败 | `VillagerAttractionMixin.profession()`、`VillagerReincarnationMixin.getOrCreateTag()` |
| 1.21.6 | VillagerReincarnation 门控边界 | `VillagerAttractionMixin.profession()`（已修复为 record-style）、`FoodDataMixin`、`RecipeRuleObserver.reloadResources` |
| 1.21.8 | 当前编译目标 | 应通过（基线对照） |
| 1.21.11 | RecipeManager 门控边界 | `RecipeManagerMixin.prepare` 签名、`RecipeMap.create`、`Recipe.CODEC` |

**Carpet 版本映射**（构建时填入 `gradle.properties`）：
- 1.21.0 → `1.21-1.4.147+v240613`
- 1.21.4 → `1.21.4-1.4.161+...`（具体 build 号查询 masa maven）
- 1.21.6 → `1.21.6-1.4.176+...`
- 1.21.8 → 保持当前 `1.21.7-1.4.177+v250630`（Carpet 1.21.7 jar 兼容 1.21.8）
- 1.21.11 → `1.21.11-1.4.194+v251223`

### 阶段 C：撰写兼容性测试报告

输出一份结构化报告 `compatibility-report-1.21.x.md`，包含：

1. **执行摘要**：支持矩阵总览（绿/黄/红）
2. **按版本详细结果**：每个 1.21.x 版本的支持程度
3. **问题清单**：分严重 bug / 轻微瑕疵 / 已知限制
4. **修复建议**（仅供参考，本次不实施）

---

## 关键文件

**配置文件**（测试时临时修改，测试后恢复）：
- [gradle.properties](file:///D:/Ddesketp/agent/carpet/gradle.properties)

**版本门控**（理解核心）：
- [VersionedMixinPlugin.java](file:///D:/Ddesketp/agent/carpet/src/main/java/carpetvpladdition/mixin/VersionedMixinPlugin.java)

**高风险 Mixin**（验证重点）：
- [VillagerAttractionMixin.java](file:///D:/Ddesketp/agent/carpet/src/main/java/carpetvpladdition/mixin/VillagerAttractionMixin.java) — `.profession()` 调用
- [FastLeafDecayMixin.java](file:///D:/Ddesketp/agent/carpet/src/main/java/carpetvpladdition/mixin/FastLeafDecayMixin.java) — `ScheduledTickAccess`
- [ObserverTickControlMixin.java](file:///D:/Ddesketp/agent/carpet/src/main/java/carpetvpladdition/mixin/ObserverTickControlMixin.java) — 第一个 `@ModifyArg` 无 `require=0`
- [FoodDataMixin.java](file:///D:/Ddesketp/agent/carpet/src/main/java/carpetvpladdition/mixin/FoodDataMixin.java) — `Mth.clamp`
- [CompoundTagValueOutput.java](file:///D:/Ddesketp/agent/carpet/src/main/java/carpetvpladdition/util/CompoundTagValueOutput.java) — `ValueOutput` 接口
- [RecipeManagerMixin.java](file:///D:/Ddesketp/agent/carpet/src/main/java/carpetvpladdition/mixin/RecipeManagerMixin.java) — 1.21.11 门控
- [RecipeRuleObserver.java](file:///D:/Ddesketp/agent/carpet/src/main/java/carpetvpladdition/settings/RecipeRuleObserver.java) — `reloadResources` 签名
- [fabric.mod.json](file:///D:/Ddesketp/agent/carpet/src/main/resources/fabric.mod.json) — 依赖声明矛盾

**历史构建错误**（参考）：
- [build-error-1.21.4.txt](file:///D:/Ddesketp/agent/carpet/build-error-1.21.4.txt)
- [build-error.txt](file:///D:/Ddesketp/agent/carpet/build-error.txt)
- [build-error-26.1.txt](file:///D:/Ddesketp/agent/carpet/build-error-26.1.txt)
- [build-error-master.txt](file:///D:/Ddesketp/agent/carpet/build-error-master.txt)

---

## 执行步骤

1. **备份 `gradle.properties`** 到 `gradle.properties.bak`
2. **构建测试 5 个版本**（按顺序）：
   - 备份当前 → 改 `minecraft_version`/`fabric_version`/`carpet_version` → `./gradlew clean build --no-daemon`（dangerouslyDisableSandbox: true，因需联网下载依赖）→ 捕获 stdout 到 `build-test-<mcver>.txt` → 恢复 `gradle.properties`
3. **恢复 `gradle.properties`** 原值，与备份比对确认一致
4. **分析构建日志**：标记编译错误、Mixin 注入失败、运行时崩溃（若能跑到 runtime）
5. **撰写报告**：`compatibility-report-1.21.x.md` 输出到 carpet 根目录

## 验证（Verification）

- 测试结束后，`gradle.properties` 内容与开始时完全一致（用 `git diff` 验证）
- 报告中每条结论都有证据支撑（构建日志行号 / 代码行号 / Carpet 版本映射）
- 5 个版本的构建结果矩阵清晰呈现（PASS / FAIL + 失败原因 + 影响 Mixin 列表）

## 已知限制

- 构建测试只能验证**编译期**和 **Mixin 应用阶段**错误，无法验证**运行时**行为（如 `ordinal` 偏移导致的逻辑错误、`AbstractMethodError` 等），这些需在游戏中实测
- masa.dy.fi maven 可能因网络问题无法访问某些旧版本 Carpet，遇到时降级为纯静态分析结论
- 部分中间版本（1.21.1、1.21.2、1.21.3、1.21.5、1.21.7、1.21.9、1.21.10）不实际构建，用静态分析推断（基于相邻版本的构建结果 + 代码审计）
