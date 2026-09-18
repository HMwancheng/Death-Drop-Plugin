package com.deathdrop;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 统计玩家短时间内的连续死亡次数，用于计算掉落几率倍数
 */
public class DeathTracker {

    private final DeathDropPlugin plugin;
    private final Map<UUID, Deque<Long>> deathTimes = new HashMap<>();

    public DeathTracker(DeathDropPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 记录一次死亡，并返回当前应使用的掉落几率倍数。
     * 会先清理超出时间窗口的旧记录。
     */
    public double onDeath(UUID uuid) {
        PluginConfig cfg = plugin.getPluginConfig();
        long now = System.currentTimeMillis();

        if (!cfg.isConsecutiveEnabled()) {
            return 1.0;
        }

        Deque<Long> times = deathTimes.computeIfAbsent(uuid, k -> new ArrayDeque<>());
        long windowMillis = cfg.getWindowSeconds() * 1000L;

        // 清理窗口外的时间点
        while (!times.isEmpty() && now - times.peekFirst() > windowMillis) {
            times.pollFirst();
        }

        // 记录本次死亡
        times.offerLast(now);

        return cfg.getMultiplier(times.size());
    }

    public void clear() {
        deathTimes.clear();
    }
}