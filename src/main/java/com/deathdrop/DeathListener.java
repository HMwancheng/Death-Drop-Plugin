package com.deathdrop;

import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 死亡掉落核心逻辑
 */
public class DeathListener implements Listener {

    private final DeathDropPlugin plugin;
    private final Random random = new Random();

    public DeathListener(DeathDropPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PluginConfig cfg = plugin.getPluginConfig();

        // 1. 记录死亡（供指令冷却与连续死亡计数使用），该部分不受掉落开关影响
        plugin.getCommandCooldown().onDeath(player);

        // 2. 掉落功能是否启用
        if (!cfg.isDropEnabled()) {
            return;
        }

        // 权限完全免疫掉落
        if (cfg.isEnableDropBypass() && player.hasPermission("deathdrop.drop.bypass")) {
            return;
        }

        // 3. 判断世界当前是否开启死亡不掉落
        boolean worldKeeps = Boolean.TRUE.equals(
                player.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY));

        // 4. 是否应用插件掉落规则
        boolean apply;
        if (cfg.isFollowWorldRule()) {
            // 跟随世界规则：仅当世界开启死亡不掉落时才干预
            apply = worldKeeps;
        } else {
            // 插件规则优先：始终干预
            apply = true;
        }
        if (!apply) {
            return; // 交给原版处理
        }

        // 5. 计算最终掉落几率（基础几率 x 连续死亡倍数），并封顶
        double multiplier = 1.0;
        if (cfg.isConsecutiveEnabled()) {
            multiplier = plugin.getDeathTracker().onDeath(player.getUniqueId());
        }
        double finalChance = Math.min(
                cfg.getChance() * multiplier,
                Math.max(cfg.getMaxChance(), 0));
        // 几率为 0 时直接返回
        if (finalChance <= 0) {
            return;
        }

        // 6. 计算哪些物品要掉落
        List<ItemStack> toDrop = new ArrayList<>();

        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            // 白名单物品插件不处理（跟随世界规则），在“世界原本全掉落”的场景下特判单独处理
            if (cfg.isWhitelisted(item.getType())) {
                if (!worldKeeps) {
                    // 世界原本会掉落（keepInventory=false），保持原版行为：白名单物品照常掉落
                    toDrop.add(item.clone());
                    inventory.clear(i);
                }
                // 世界 keepInventory=true 时白名单物品本就保留，无需处理
                continue;
            }

            // 计算该物品应掉落的数量
            int dropAmount = computeDropAmount(item, finalChance, cfg.isPerItem());
            if (dropAmount <= 0) {
                continue;
            }

            ItemStack drop = item.clone();
            drop.setAmount(dropAmount);
            toDrop.add(drop);

            int remaining = item.getAmount() - dropAmount;
            if (remaining <= 0) {
                inventory.clear(i);
            } else {
                item.setAmount(remaining);
                inventory.setItem(i, item);
            }
        }

        // 7. 落地规则处理：若世界原本不开死亡不掉落（正常全掉落），
        //    为了体现“插件规则优先”（保留未命中物品），需将原版掉落改为保留
        if (!worldKeeps) {
            event.setKeepInventory(true);
            event.getDrops().clear();
        }

        // 8. 实际丢出物品
        if (!toDrop.isEmpty()) {
            Location loc = player.getLocation();
            for (ItemStack drop : toDrop) {
                player.getWorld().dropItemNaturally(loc, drop);
            }
        }
    }

    /**
     * 计算一个物品堆叠本次应掉落的数量
     */
    private int computeDropAmount(ItemStack item, double chancePerHundred, boolean perItem) {
        int amount = item.getAmount();
        if (perItem) {
            // 逐件判定：每个物品单独 roll 一次
            int hits = 0;
            for (int k = 0; k < amount; k++) {
                if (roll(chancePerHundred)) {
                    hits++;
                }
            }
            return hits;
        } else {
            // 整组判定：命中则整组掉落
            return roll(chancePerHundred) ? amount : 0;
        }
    }

    /** 以百分比几率进行一次判定 */
    private boolean roll(double chancePerHundred) {
        return random.nextDouble() * 100.0 < chancePerHundred;
    }
}