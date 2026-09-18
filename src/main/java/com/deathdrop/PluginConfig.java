package com.deathdrop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

/**
 * 配置文件读取与封装
 */
public class PluginConfig {

    private FileConfiguration config;

    // 掉落
    private boolean dropEnabled;
    private double chance;
    private double maxChance;
    private boolean followWorldRule;
    private boolean perItem;
    private boolean enableDropBypass;

    // 连续死亡
    private boolean consecutiveEnabled;
    private String consecutiveMode;
    private long windowSeconds;
    private TreeMap<Integer, Double> multiplierMap;

    // 白名单
    private boolean whitelistEnabled;
    private Set<Material> whitelist;

    // 指令冷却
    private boolean cooldownEnabled;
    private long cooldownSeconds;
    private Set<String> blockedCommands;
    private String blockMessage;

    public PluginConfig(DeathDropPlugin plugin) {
        reload();
    }

    public void reload() {
        config = Bukkit.getPluginManager().getPlugin("DeathDrop").getConfig();

        ConfigurationSection drop = config.getConfigurationSection("drop");
        dropEnabled = drop.getBoolean("enabled", true);
        chance = drop.getDouble("chance", 5.0);
        maxChance = drop.getDouble("max-chance", 100.0);
        followWorldRule = drop.getBoolean("follow-world-rule", false);
        perItem = drop.getBoolean("per-item", true);
        enableDropBypass = drop.getBoolean("enable-bypass-permission", true);

        ConfigurationSection cd = config.getConfigurationSection("consecutive-death");
        consecutiveEnabled = cd.getBoolean("enabled", true);
        consecutiveMode = cd.getString("mode", "sliding").trim().toLowerCase();
        windowSeconds = cd.getLong("window-seconds", 120);
        multiplierMap = new TreeMap<>();
        ConfigurationSection mult = cd.getConfigurationSection("multiplier");
        if (mult != null) {
            for (String key : mult.getKeys(false)) {
                try {
                    multiplierMap.put(Integer.parseInt(key), mult.getDouble(key, 1.0));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (multiplierMap.isEmpty()) {
            multiplierMap.put(1, 1.0);
        }

        ConfigurationSection wl = config.getConfigurationSection("whitelist-item");
        whitelistEnabled = wl.getBoolean("enabled", true);
        whitelist = new HashSet<>();
        if (whitelistEnabled) {
            for (String item : wl.getStringList("items")) {
                Material m = parseMaterial(item);
                if (m != null) {
                    whitelist.add(m);
                }
            }
        }

        ConfigurationSection ccmd = config.getConfigurationSection("death-command-cooldown");
        cooldownEnabled = ccmd.getBoolean("enabled", true);
        cooldownSeconds = ccmd.getLong("cooldown-seconds", 30);
        blockedCommands = new HashSet<>(ccmd.getStringList("blocked-commands"));
        blockMessage = ccmd.getString("block-message",
                "&c你刚刚死亡，请等待 &e%seconds% &c秒后再执行该指令！");
    }

    private Material parseMaterial(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        Material m = Material.matchMaterial(name.trim().toLowerCase());
        return m;
    }

    /** 获取连续死亡的掉落几率倍数 */
    public double getMultiplier(int deathCount) {
        Integer floor = multiplierMap.floorKey(deathCount);
        if (floor != null) {
            return multiplierMap.get(floor);
        }
        // 次数小于任一配置项（正常不会发生，因为一般都有 1），取最小值
        return multiplierMap.entrySet().iterator().next().getValue();
    }

    public boolean isWhitelisted(Material material) {
        return whitelistEnabled && whitelist.contains(material);
    }

    // ------- getters -------

    public boolean isDropEnabled() { return dropEnabled; }
    public double getChance() { return chance; }
    public double getMaxChance() { return maxChance; }
    public boolean isFollowWorldRule() { return followWorldRule; }
    public boolean isPerItem() { return perItem; }
    public boolean isEnableDropBypass() { return enableDropBypass; }

    public boolean isConsecutiveEnabled() { return consecutiveEnabled; }
    /** 连续死亡判定是否为“刷新式”，否则为“滑动窗口” */
    public boolean isRefreshMode() { return "refresh".equals(consecutiveMode); }
    public long getWindowSeconds() { return windowSeconds; }

    public boolean isWhitelistEnabled() { return whitelistEnabled; }

    public boolean isCooldownEnabled() { return cooldownEnabled; }
    public long getCooldownSeconds() { return cooldownSeconds; }
    public Set<String> getBlockedCommands() { return blockedCommands; }
    public String getBlockMessage() { return blockMessage; }

    public FileConfiguration getRawConfig() { return config; }
}