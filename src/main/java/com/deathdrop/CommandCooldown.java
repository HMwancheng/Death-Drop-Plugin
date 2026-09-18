package com.deathdrop;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 死亡后指令冷却管理
 */
public class CommandCooldown {

    private final DeathDropPlugin plugin;
    private final Map<UUID, Long> deathTimes = new HashMap<>();

    public CommandCooldown(DeathDropPlugin plugin) {
        this.plugin = plugin;
    }

    /** 玩家死亡时调用，重置该玩家冷却计时起点 */
    public void onDeath(Player player) {
        deathTimes.put(player.getUniqueId(), System.currentTimeMillis());
    }

    /** 该玩家是否处于指令冷却中 */
    public boolean isBlocked(Player player) {
        PluginConfig cfg = plugin.getPluginConfig();
        if (!cfg.isCooldownEnabled()) {
            return false;
        }
        Long deathTime = deathTimes.get(player.getUniqueId());
        if (deathTime == null) {
            return false;
        }
        return remainingMillis(player) > 0;
    }

    /** 剩余冷却毫秒（0 表示已结束） */
    public long remainingMillis(Player player) {
        Long deathTime = deathTimes.get(player.getUniqueId());
        if (deathTime == null) {
            return 0;
        }
        long cdMillis = plugin.getPluginConfig().getCooldownSeconds() * 1000L;
        long remain = cdMillis - (System.currentTimeMillis() - deathTime);
        if (remain <= 0) {
            deathTimes.remove(player.getUniqueId());
            return 0;
        }
        return remain;
    }

    /** 剩余冷却秒数（向上取整，用于消息展示） */
    public long remainingSeconds(Player player) {
        return (long) Math.ceil(remainingMillis(player) / 1000.0);
    }

    public void clear() {
        deathTimes.clear();
    }
}