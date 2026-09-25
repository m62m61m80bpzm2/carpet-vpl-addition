# Carpet VPL Addition

一个基于 `fabric-carpet` 的 Fabric 服务端 Mod，为原版 Carpet 追加 **46 条**自定义游戏规则：区块加载矿车、物品堆叠、合成配方、村民与生物行为、紫水晶与作物生长、鞘翅与信标机制，外加几个原版 bug 修复。

规则**默认全部关闭**，用 `/carpet <规则名> true` 按需开启；只在服务端安装即可，玩家客户端什么都不用装。

> **声明：本项目的代码（含全部 Mixin、规则、工具类）均由 AI 生成，特此说明。**

* 目标游戏版本：**Minecraft 26.1 ~ 26.3**
* 当前 Mod 版本：**b1.16.1.1**（测试版，已扩到支持 26.3；最新正式版为 v1.15.1）
* 支持语言：简体中文 / 繁體中文 / English / Français / Español (AR) / Português (BR)

本文件是**给玩家使用的说明**（怎么装、有哪些规则）。
如果你想修改这个 Mod 源码 / 参与开发，请看文档 → [dev.md](./dev.md)（开发文档）。

\---

## 目录

* [环境依赖](#环境依赖)
* [安装](#安装)
* [使用](#使用)
* [规则列表](#规则列表)
* [已知问题](#已知问题)

## 环境依赖

|依赖|版本要求|
|-|-|
|Minecraft|`>=26.1 <26.4`|
|Java|`>=25`|
|Fabric Loader|`>=0.19.3`|
|Fabric API|任意|
|Carpet|`>=26.1`|

## 安装

1. 安装 [Fabric Loader](https://fabricmc.net/use/installer/)（0.19.3+）。
2. 将以下 Mod 放入 `.minecraft/mods/` 文件夹：

   * Carpet（26.1+ 版本）
   * Fabric API
   * **`[vpl-b1.16.1.1-for-26.1-26.3]carpet-addition-b1.16.1.1.jar`**
3. 启动游戏即可。

## 使用

所有规则都通过 Carpet 自带的 `/carpet` 命令来查看和设置：

```
/carpet list carpet-vpl-addition     查看本 Mod 的全部规则及当前值
/carpet <规则名> <值>                 设置某个规则（例如 /carpet cactusBoneMeal true）
```

部分规则设置后**立即生效**；标注“需要重进世界生效”的属性类规则，设置后重进存档即可。

## 规则列表

### 玩家属性

|规则|类型|默认值|说明|
|-|-|-|-|
|`maxPlayerHealth`|数值|20|玩家最大生命值上限|
|`playerAttackDamage`|数值|2.0|玩家基础攻击力|
|`maxAir`|数值|300|玩家最大水下氧气值（游戏刻）|

### 物品堆叠

|规则|类型|默认值|说明|
|-|-|-|-|
|`stackableTotem`|布尔|false|不死图腾可堆叠至 64|
|`stackableLavaBucket`|布尔|false|岩浆桶可堆叠至 64|
|`stackableGlassBottle`|布尔|false|空瓶可堆叠至 64|
|`stackableWaterBucket`|布尔|false|水桶可堆叠至 64|
|`stackablePotion`|布尔|false|药水可堆叠至 64|
|`stackableStew`|布尔|false|炖菜可堆叠至 64|
|`stackableCake`|布尔|false|蛋糕可堆叠至 64|
|`hopperMinecartStackSize`|数值|1|漏斗矿车堆叠数（上限 99）|

### 合成与经验

|规则|类型|默认值|说明|
|-|-|-|-|
|`totemRecipe`|布尔|false|不死图腾合成配方|
|`calciteRecipe`|布尔|false|方解石合成：安山岩+骨粉=2 方解石|
|`tuffRecipe`|布尔|false|凝灰岩合成：圆石+骨粉=2 凝灰岩|
|`woolToString`|布尔|false|羊毛分解：任意羊毛 1=线 4|
|`quartzUnpack`|布尔|false|石英块分解：石英块 1=下界石英 4|
|`fixedXpPerLevel`|布尔|false|每级经验固定 62|
|`furnaceXpMultiplier`|数值|1.0|熔炉经验倍率|

### 日常修复

|规则|类型|默认值|说明|
|-|-|-|-|
|`anvilNoDamage`|布尔|false|铁砧使用不损坏|
|`lavaSourceOnLapisOre`|布尔|false|青金石矿石岩浆源：流动岩浆与至少 2 个岩浆源水平相邻且正下方是青金石矿石（含深层）时转化为岩浆源（无限水机制）|
|`pearlChunkKeepalive`|布尔|false|珍珠区块保活：修复重进游戏后飞行中的末影珍珠所在区块偶发不加载的原版 bug|

### 村民与生物

|规则|类型|默认值|说明|
|-|-|-|-|
|`villagerGolem`|布尔|false|绿宝石块+雕刻南瓜召唤村民|
|`villagerAttraction`|布尔|false|村民被特定物品吸引|
|`villagerReincarnation`|布尔|false|村民死亡掉落刷怪蛋转世|
|`villagerNoPriceOnAttack`|布尔|false|村民被攻击不涨价|
|`bedrockPortalZombiePigman`|布尔|false|地狱门刷僵尸猪人（基岩版行为）|
|`tradedZombieVillagerPeace`|布尔|false|和平模式不再移除交易过的僵尸村民|
|`beeNoAngerOnHarvest`|布尔|false|采蜜不惊蜂：剪蜜脾/玻璃瓶取蜜时蜜蜂不仇恨玩家（等同蜂巢下永远压着点燃的营火；砸毁蜂巢与爆炸仍会仇恨）|

### 方块与生长

|规则|类型|默认值|说明|
|-|-|-|-|
|`fastLeafDecay`|布尔|false|树叶快速腐烂|
|`disableSnow`|布尔|false|禁止自然下雪|
|`disableKelpGrowth`|布尔|false|禁止海带生长|
|`cactusGrowthMultiplier`|数值|1.0|仙人掌生长速度倍率|
|`cactusBoneMeal`|布尔|false|仙人掌可被骨粉催熟|
|`bedrockSugarcaneBonemeal`|布尔|false|甘蔗可被骨粉催熟（基岩版行为）|
|`protectImmatureCrops`|布尔|false|保护未成熟农作物|
|`minableBuddingAmethyst`|布尔|false|精准采集可挖紫水晶母岩（普通镐仍为原版行为）|
|`amethystBoneMeal`|布尔|false|骨粉催熟紫水晶：对**紫水晶母岩**用骨粉立刻长出新芽，对芽用则推进一档（玩家与发射器均生效，仅影响水晶）|

### 红石与机制

|规则|类型|默认值|说明|
|-|-|-|-|
|`observerTickDelay`|数值|2|侦测器触发延迟|
|`bedrockPushableFurnace`|布尔|false|熔炉可被活塞推动|
|`tridentVoidReturn`|布尔|false|忠诚三叉戟虚空返回|
|`beaconUnifiedPPUpdate`|布尔|false|信标统一PP更新（20GT）|
|`noZombieHorseSpawn`|布尔|false|禁止僵尸马自然生成（**仅 26.2 生效**，26.1/26.1.2 上无效）|
|`namedMinecartChunkLoader`|布尔|false|命名矿车区块加载器：铁砧把矿车命名为 `chunkloader`（或 6 语言等价名，忽略大小写/空格/下划线）后，以它为中心的 3×3 区块保持完全运行，并随矿车移动|
|`elytraFireworkMultiplier`|数值|1.0|鞘翅烟花巡航速度倍率（缩放原版推进目标速度，加速平滑不抖动；不改烟花飞行与爆炸伤害）|

### 修复

|规则|类型|默认值|说明|
|-|-|-|-|
|`stringDupe`|布尔|false|重新引入刷线机漏洞|
|`pickBlockNbt`|布尔|true|修复 Ctrl+中键复制方块 NBT（26.1-26.2）|

## 已知问题

* 已对 26.1.2 / 26.2 / 26.3 做注入点核对（91 个被引用原版类全部存在、221 处成员引用无回归、38 个 Mixin 目标类齐全），并用 26.3 映射试编译验证；期间发现并修复两处静态核对看不出的破坏：`BonemealableBlock` 在 26.3 加了 `BonemealSource` 参数（仙人掌骨粉已改为注入 `BoneMealItem.growCrop`，三版通用）、`RecipeManager.prepare()` 与 `registries` 字段在 26.3 被删除（5 条配方规则已在 26.3+ 自动关闭，不再崩溃）。**26.1.2 与 26.2 已真起服务端验证通过**（正常启动 + 规则实时开关）；26.3 上我方 38 个 Mixin 全部应用通过，但启动仍会失败在 Carpet 自身（其 26.3-beta-3 缺 `DensityFunction$FunctionContext`），需等 Carpet 发布 26.3 正式版。
* `noZombieHorseSpawn` 的说明目前写的是"仅 26.2 生效"；源码核对显示 26.1.2 与 26.3 同样把 `ZOMBIE_HORSE` 注册到 `Monster::checkMonsterSpawnRules`，该限定语很可能已过时，待实机确认后再改 6 语言文案。
* 与其他模组（尤其 Lithium 一类区块/性能优化模组）的搭配未做兼容实测。

