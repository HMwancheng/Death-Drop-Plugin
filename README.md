# DeathDrop-死亡掉落

Minecraft Paper（Java 高版本）服务端插件：玩家死亡时，即使世界开启了死亡不掉落，也会按几率掉落背包中的物品；并提供死亡后指令冷却功能。

## 功能

- 世界开启死亡不掉落时，仍按配置几率掉落背包物品（默认 5%）
- 可配置是否跟随世界规则（默认插件规则优先，白名单物品除外）
- 两种几率算法：
  - **连续死亡加成**：短时间连续死亡提升掉落几率，支持两种判定模式（滑动窗口 / 刷新式）
  - **逐件判定**：背包内每个物品单独计算一次几率（`per-item: true`）
- **白名单物品**：插件永不处理，掉落与否跟随世界规则
- **死亡后指令冷却**：死亡后一段时间内禁止执行指定指令（如 `/back`），并提示剩余时间

## 配置

配置文件为插件目录下的 `config.yml`，关键项：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `drop.chance` | `5` | 基础掉落几率（百分比） |
| `drop.max-chance` | `100` | 最终几率上限，防止连续死亡叠加过高 |
| `drop.follow-world-rule` | `false` | `false`=插件规则优先；`true`=仅世界开启死亡不掉落时插件才生效 |
| `drop.per-item` | `true` | `true`=每个物品单独判定；`false`=整组判定 |
| `consecutive-death.mode` | `sliding` | `sliding`=滑动窗口；`refresh`=刷新式（每次死亡重置窗口） |
| `consecutive-death.window-seconds` | `120` | 连续死亡时间窗口（秒） |
| `consecutive-death.multiplier` | 见文件 | 死亡次数 → 几率倍数 |
| `whitelist-item.items` | 鞘翅、龙蛋 | 白名单物品（带命名空间 ID） |
| `death-command-cooldown.enabled` | `true` | 是否启用指令冷却 |
| `death-command-cooldown.cooldown-seconds` | `30` | 冷却时长（秒） |
| `death-command-cooldown.blocked-commands` | `back` | 冷却期间禁用的指令（前缀匹配，无需带 `/`） |
| `death-command-cooldown.block-message` | 见文件 | 拦截提示，支持 `%seconds%` 占位符 |

## 命令 / 权限

| 命令 | 说明 | 权限 |
| --- | --- | --- |
| `/deathdrop reload` | 热重载配置 | `deathdrop.admin`（默认 OP） |

| 权限 | 说明 |
| --- | --- |
| `deathdrop.drop.bypass` | 完全免疫插件掉落 |
| `deathdrop.command.bypass` | 免疫死亡后指令冷却 |

## 构建

本项目通过 GitHub Actions 自动编译，产物以上传为 workflow artifact。也可本地构建：

```bash
gradle build
```

产物位于 `build/libs/DeathDrop-1.0.0.jar`。
