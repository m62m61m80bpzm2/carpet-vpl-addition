# Carpet VPL Addition - 开发文档

## 版本号
当前版本：1.11.0（每次修改后 +1）

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
│   ├── TripwireHookBlockStringDupeMixin.java # 刷线机
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

## 已知问题
- 无
