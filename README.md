# Carpet VPL Addition

一个基于 `fabric-carpet` 的 Fabric 服务端 Mod，为原版 Carpet 添加大量自定义游戏规则（Rules）。

* 目标游戏版本：**Minecraft 26.1 ~ 26.2**
* 当前 Mod 版本：**b1.14.3.7**（测试版）
* 支持语言：简体中文 / English

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
|Minecraft|`>=26.1 <26.3`|
|Java|`>=25`|
|Fabric Loader|`>=0.19.3`|
|Fabric API|任意|
|Carpet|`>=26.1`|

## 安装

1. 安装 [Fabric Loader](https://fabricmc.net/use/installer/)（0.19.3+）。
2. 将以下 Mod 放入 `.minecraft/mods/` 文件夹：

   * Carpet（26.1+ 版本）
   * Fabric API
   * **`[vpl-b1.14.3.7-for-26.1-26.2]carpet-addition-b1.14.3.7.jar`**
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
|`fixedXpPerLevel`|布尔|false|每级经验固定 62|
|`furnaceXpMultiplier`|数值|1.0|熔炉经验倍率|

### 村民与生物

|规则|类型|默认值|说明|
|-|-|-|-|
|`villagerGolem`|布尔|false|绿宝石块+雕刻南瓜召唤村民|
|`villagerAttraction`|布尔|false|村民被特定物品吸引|
|`villagerReincarnation`|布尔|false|村民死亡掉落刷怪蛋转世|
|`villagerNoPriceOnAttack`|布尔|false|村民被攻击不涨价|
|`bedrockPortalZombiePigman`|布尔|false|地狱门刷僵尸猪人（基岩版行为）|

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

### 红石与机制

|规则|类型|默认值|说明|
|-|-|-|-|
|`observerTickDelay`|数值|2|侦测器触发延迟|
|`bedrockPushableFurnace`|布尔|false|熔炉可被活塞推动|
|`tridentVoidReturn`|布尔|false|忠诚三叉戟虚空返回|
|`beaconUnifiedPPUpdate`|布尔|false|信标统一PP更新（20GT）|
|`noZombieHorseSpawn`|布尔|false|禁止僵尸马自然生成|

### 修复

|规则|类型|默认值|说明|
|-|-|-|-|
|`stringDupe`|布尔|false|重新引入刷线机漏洞|
|`pickBlockNbt`|布尔|true|修复 Ctrl+中键复制方块 NBT（26.1-26.2）|

## 已知问题

* 无

