# Carpet VPL Addition

一个基于 [fabric-carpet](https://github.com/gnembon/fabric-carpet) 的 [Fabric](https://fabricmc.net/) 服务端 Mod，为原版 Carpet 添加大量自定义游戏规则（Rules）。

- 目标版本：**Minecraft 26.2**
- 当前版本：**1.14.0**
- 技术栈：Fabric Loader 0.19.3 + Fabric API 0.154.2+26.2 + fabric-loom + Mojang mappings + Java 25

## 功能概览

共 **39** 条规则，涵盖：
- 🧟 生存便利（村民、实体磨制、刷怪、随机数）
- 🛠 物品堆叠修改
- 🌱 农作物 / 方块生长控制
- ⚙️ 红石与机制修正
- 🐛 原版漏洞修复（刷线机、中键复制 NBT）

## 环境依赖

| 依赖 | 版本 |
|---|---|
| Minecraft | `>=26.2 <26.3` |
| Fabric Loader | `>=0.19.3` |
| Java | `>=25` |
| Fabric API | `*` |
| Carpet | `>=26.2` |

## 构建

```bash
./gradlew build
```

产物位于 `build/libs/`：
- `[vpl26.2]carpet-vpl-addition-<version>.jar` — 发布用 jar
- `[vpl26.2]carpet-vpl-addition-<version>-sources.jar` — 源码 jar

## 安装

1. 安装 [Fabric Loader](https://fabricmc.net/use/installer/)（0.19.3+）
2. 将以下 Mod 放入 `mods/` 文件夹：
   - Carpet（26.2 版本）
   - Fabric API
   - **Carpet VPL Addition**
3. 启动游戏

## 使用

所有规则通过 Carpet 的 `/carpet` 命令查看与修改：

```
/carpet list carpet_vpl_addition   查看本 Mod 全部规则
/carpet name <value>               设置某个规则的值
```

## 规则列表

### 玩家属性

| 规则 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `maxPlayerHealth` | 数值 | 20 | 玩家最大生命值上限 |
| `playerAttackDamage` | 数值 | 2.0 | 玩家基础攻击力 |
| `maxArmor` | 数值 | 0 | 玩家基础护甲值 |
| `maxAir` | 数值 | 300 | 玩家最大水下氧气值（游戏刻） |
| `maxSaturation` | 数值 | 20 | 玩家最大饱食度上限 |

### 物品堆叠

| 规则 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `stackableTotem` | 布尔 | false | 不死图腾可堆叠至 64 |
| `stackableLavaBucket` | 布尔 | false | 岩浆桶可堆叠至 64 |
| `stackableBucket` | 布尔 | false | 空桶可堆叠至 64 |
| `stackableGlassBottle` | 布尔 | false | 空瓶可堆叠至 64 |
| `stackableWaterBucket` | 布尔 | false | 水桶可堆叠至 64 |
| `stackableMusicDisc` | 布尔 | false | 唱片可堆叠至 64 |
| `stackableMilkBucket` | 布尔 | false | 奶桶可堆叠至 64 |
| `stackablePowderSnowBucket` | 布尔 | false | 细雪桶可堆叠至 64 |
| `stackableEnderPearl` | 布尔 | false | 末影珍珠可堆叠至 64 |
| `stackableSign` | 布尔 | false | 告示牌可堆叠至 64 |
| `stackablePotion` | 布尔 | false | 药水可堆叠至 64 |
| `stackableStew` | 布尔 | false | 炖菜可堆叠至 64 |
| `stackableCake` | 布尔 | false | 蛋糕可堆叠至 64 |
| `hopperMinecartStackSize` | 数值 | 1 | 漏斗矿车堆叠数（上限 99） |

### 合成与经验

| 规则 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `totemRecipe` | 布尔 | false | 不死图腾合成配方 |
| `fixedXpPerLevel` | 布尔 | false | 每级经验固定 62 |
| `furnaceXpMultiplier` | 数值 | 1.0 | 熔炉经验倍率 |

### 村民与生物

| 规则 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `villagerGolem` | 布尔 | false | 绿宝石块+雕刻南瓜召唤村民 |
| `villagerAttraction` | 布尔 | false | 村民被特定物品吸引 |
| `villagerReincarnation` | 布尔 | false | 村民死亡掉落刷怪蛋转世 |
| `villagerNoPriceOnAttack` | 布尔 | false | 村民被攻击不涨价 |
| `bedrockPortalZombiePigman` | 布尔 | false | 地狱门刷僵尸猪人（基岩版行为） |

### 方块与生长

| 规则 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `fastLeafDecay` | 布尔 | false | 树叶快速腐烂 |
| `disableSnow` | 布尔 | false | 禁止自然下雪/结冰 |
| `disableKelpGrowth` | 布尔 | false | 禁止海带生长 |
| `cactusGrowthMultiplier` | 数值 | 1.0 | 仙人掌生长速度倍率 |
| `cactusBoneMeal` | 布尔 | false | 仙人掌可被骨粉催熟 |
| `bedrockSugarcaneBonemeal` | 布尔 | false | 甘蔗可被骨粉催熟（基岩版行为） |
| `protectImmatureCrops` | 布尔 | false | 保护未成熟农作物 |

### 红石与机制

| 规则 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `observerTickDelay` | 数值 | 2 | 侦测器触发延迟 |
| `bedrockPushableFurnace` | 布尔 | false | 熔炉可被活塞推动 |
| `tridentVoidReturn` | 布尔 | false | 忠诚三叉戟虚空返回 |

### 修复

| 规则 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `stringDupe` | 布尔 | false | 重新引入刷线机漏洞 |
| `pickBlockNbt` | 布尔 | true | 修复 Ctrl+中键复制方块 NBT |

## 已知问题

- 无

## 开发文档

详见 [dev.md](./dev.md)。