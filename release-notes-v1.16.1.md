# Carpet VPL Addition v1.16.1

适用版本：Minecraft 26.1 / 26.1.2 / 26.2 / 26.3（Fabric 服务端，一个 jar 通吃）

## 新增规则

| 规则 | 默认值 | 说明 |
|------|--------|------|
| `villagerNoWitchFromLightning` | false | 雷电不劈村民成女巫：村民被闪电击中不再变身女巫，只受正常雷击伤害 |

## 兼容版本

- 支持 Minecraft 版本从 26.1~26.2 扩展到 **26.1 ~ 26.3**，一个 jar 通吃
- 依赖 Carpet 26.3 正式版（v261008）适配；26.1/26.2 环境继续用各版本官方 Carpet
- 26.3 的配方装载机制改版已适配：5 条配方规则（totemRecipe / calciteRecipe / tuffRecipe / woolToString / quartzUnpack）在 26.3 走动态数据包方案，开关即时生效
- 修复 MC 26.2 环境下启动崩溃的问题（内置 MixinExtras 升级至 0.5.5）

## 安装要求

| 依赖 | 版本要求 |
|------|----------|
| Minecraft | `>=26.1 <26.4` |
| Java | `>=25` |
| Fabric Loader | `>=0.19.3` |
| Fabric API | 任意 |
| Carpet | `>=26.1`（26.2 / 26.3 请配对应版本的官方 Carpet） |

将 jar 放入服务端 `mods/` 文件夹，规则开关：`/carpet <规则名> true`
