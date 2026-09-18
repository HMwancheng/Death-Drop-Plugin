package com.deathdrop;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 统计玩家短时间内的连续死亡次数，用于计算掉落几率倍数。
 * 支持两种判定模式（config.yml 的 consecutive-death.mode）：
 *   sliding = 滑动窗口：只统计“距当前死亡时刻 window-seconds 秒内”的死亡次数；
 *   refresh = 刷新式：以上一次死亡为锚点，两次死亡间隔不超过窗口即算连续，每次死亡刷新计时起点。
 */
public class DeathTracker {

    private final DeathDropPlugin plugin;

    // sliding 模式：每个玩家的死亡时间队列
    private final Map<UUID, Deque<Long>> deathTimes = new HashMap<>();
    // refresh 模式：每个玩家的上次死亡时间与连续次数
    private final Map<UUID, Long> lastDeathTime = new HashMap<>();
    private final Map<UUID, Integer> streakCount = new HashMap<>();

    public DeathTracker(DeathDropPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 记录一次死亡，并返回当前应使用的掉落几率倍数。
     */
    public double onDeath(UUID uuid) {
        PluginConfig cfg = plugin.getPluginConfig();
        long now = System.currentTimeMillis();

        if (!cfg.isConsecutiveEnabled()) {
            return 1.0;
        }

        long windowMillis = cfg.getWindowSeconds() * 1000L;
        int count = cfg.isRefreshMode()
                ? recordRefresh(uuid, now, windowMillis)
                : recordSliding(uuid, now, windowMillis);

        return cfg.getMultiplier(count);
    }

    /** sliding：窗口锚定当前时刻，清掉超过窗口的旧记录后计数 */
    private int recordSliding(UUID uuid, long now, long windowMillis) {
        Deque<Long> times = deathTimes.computeIfAbsent(uuid, k -> new ArrayDeque<>());
        while (!times.isEmpty() && now - times.peekFirst() > windowMillis) {
            times.pollFirst();
        }
        times.offerLast(now);
        return times.size();
    }

    /** refresh：以上次死亡为锚点，间隔超过窗口则重置为 1，否则次数 +1 并刷新计时起点 */
    private int recordRefresh(UUID uuid, long now, long windowMillis) {
        Long last = lastDeathTime.get(uuid);
        int count;
        if (last == null || now - last > windowMillis) {
            count = 1;
        } else {
            count = streakCount.getOrDefault(uuid, 0) + 1;
        }
        lastDeathTime.put(uuid, now);
        streakCount.put(uuid, count);
        return count;
    }

    public void clear() {
        deathTimes.clear();
        lastDeathTime.clear();
        streakCount.clear();
    }
}