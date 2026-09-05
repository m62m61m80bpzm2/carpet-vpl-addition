# 待修复：noZombieHorseSpawn 在 26.1 / 26.1.2 上静默失效

> 创建时间：2026-09-04（b1.14.3.1）
> 状态：**已结案（b1.14.4.1）——处理方式：翻译中标注"仅在 26.2 生效"，不做门控，1.21.6 分支不处理**
> 影响版本：26.1、26.1.1、26.1.2（26.2 正常）

---

## 问题描述

规则 `noZombieHorseSpawn` 在 **26.2** 上正常工作，但在 **26.1 / 26.1.2** 上静默失效：
- 规则开启后无报错、无崩溃
- 僵尸马夜间依然正常自然生成
- 用户无法感知规则已关闭

## 根因

`ZombieHorseSpawnMixin` 注入的目标是 `Monster.checkMonsterSpawnRules`。

该 mixin 的逻辑假设：ZombieHorse 在 `SpawnPlacements` 中注册时使用了 `Monster::checkMonsterSpawnRules` 作为前置判断函数。

但实际源码对照结果：

| 版本 | SpawnPlacements 中 ZombieHorse 的注册情况 | mixin 是否生效 |
|------|------------------------------------------|---------------|
| **26.1** | **未注册**（SpawnPlacements 中无 ZombieHorse 条目） | ❌ 失效 |
| **26.1.2** | **未注册**（同 26.1） | ❌ 失效 |
| **26.2** | 注册为 `Monster::checkMonsterSpawnRules` | ✅ 正常 |

因此，26.1/26.1.2 上僵尸马根本不走 `Monster.checkMonsterSpawnRules` 这条路径，mixin 的注入点对僵尸马**完全无效**。

> 补充说明：1.21.6-1.21.10 分支上同样存在此问题，但更严重——该分支的 ZombieHorse 继承 `Animal`（非 `Monster`），SpawnPlacements 注册的是独立的 `checkZombieHorseSpawnRules`，mixin 目标类 `Monster.class` 与 ZombieHorse 毫无关联。

---

## 修复方案（选其一，待大版本实施）

### 方案一：VersionedMixinPlugin 门控（推荐）

在 `VersionedMixinPlugin` 中新增版本区分，26.1/26.1.2 直接禁用该 mixin：

```java
// onLoad() 中新增
if (version.startsWith("26")) {
    isAtLeast1_21_2 = true;
    isAtLeast1_21_6 = true;
    isAtLeast26_2 = !version.startsWith("26.1"); // 新增：26.2+ 才允许此 mixin
}

// shouldApplyMixin() 中新增门控
if (!isAtLeast26_2) {
    if (mixinClassName.equals("carpetvpladdition.mixin.ZombieHorseSpawnMixin")) return false;
}
```

**优点**：改动最小，干净利落，26.1/26.1.2 上不加载 mixin，不残留无效逻辑。
**缺点**：26.1/26.1.2 用户无法使用此规则（但该规则本身也无法工作，属诚实失败）。

---

### 方案二：运行时反射探测

在 mixin 初始化时反射检查 `SpawnPlacements` 中是否存在 ZombieHorse 条目，有则执行，无则跳过：

```java
private static final boolean ZOMBIE_HORSE_HAS_SPAWN_RULES = hasZombieHorseSpawnRegistration();

private static boolean hasZombieHorseSpawnRegistration() {
    try {
        // 反射检查 SpawnPlacements 中是否有 ZombieHorse 相关的注册
        // 26.2 有，26.1/26.1.2 没有
        return false; // 待补充具体实现
    } catch (Exception e) {
        return false;
    }
}

@Inject(method = "checkMonsterSpawnRules", at = @At("HEAD"), cancellable = true)
private static void onCheckMonsterSpawnRules(..., CallbackInfoReturnable<Boolean> cir) {
    if (!ZOMBIE_HORSE_HAS_SPAWN_RULES) return; // 该版本不支持
    // ... 原有逻辑
}
```

**优点**：不需要修改 VersionedMixinPlugin，逻辑内聚在 mixin 内。
**缺点**：反射开销（仅初始化时一次，可接受）；代码复杂度略高。

---

### 方案三：适配所有版本的正确拦截点

查清 26.1/26.1.2 中僵尸马的实际生成路径，改用正确的拦截点：

1. 26.1/26.1.2 中 ZombieHorse 若未被 SpawnPlacements 注册，说明其生成走的是其他路径（如 NaturalSpawner 直接调 `finalizeSpawn`）
2. 此时应回退到旧方案：注入 `ZombieHorse.finalizeSpawn`，但需确认 26.1/26.1.2 的 `NaturalSpawner` 是否会检查返回值
3. 若检查方式与 26.2 不同，需针对性适配

**优点**：理论上可实现全版本兼容。
**缺点**：需要深入分析 26.1/26.1.2 的 NaturalSpawner 逻辑，工作量大，稳定性风险高。

---

## 建议

**大版本升级时采用方案一**，理由：
- 改动最小，风险最低
- 26.1/26.1.2 上规则本就无法工作，禁用比静默失效更诚实
- 避免引入反射或复杂的版本探测逻辑

若后续需要真正支持 26.1/26.1.2，再评估方案三。

---

## 相关文件

- `src/main/java/carpetvpladdition/mixin/ZombieHorseSpawnMixin.java`
- `src/main/java/carpetvpladdition/mixin/VersionedMixinPlugin.java`
- MC 源码参考：`D:\Ddesketp\agent\mc源码\26.1\net\minecraft\world\entity\SpawnPlacements.java`
  （对比 26.2 同文件，确认 ZombieHorse 条目差异）
