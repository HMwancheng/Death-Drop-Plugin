package com.deathdrop;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * 死亡后指令冷却拦截
 */
public class CommandListener implements Listener {

    private final DeathDropPlugin plugin;

    public CommandListener(DeathDropPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        PluginConfig cfg = plugin.getPluginConfig();
        if (!cfg.isCooldownEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        if (player.hasPermission("deathdrop.command.bypass")) {
            return;
        }

        // 解析用户输入的首个指令（去掉前导“/”，并忽略“/”后的空格）
        String message = event.getMessage().trim();
        if (message.startsWith("/")) {
            message = message.substring(1);
        }
        // 取指令名（可能带命名空间前缀或子命令，如 back:claim、back 等）
        int space = message.indexOf(' ');
        String command = (space < 0) ? message : message.substring(0, space);

        // 前缀匹配被禁指令（back 会匹配 /back、/back:xxx）
        for (String blocked : cfg.getBlockedCommands()) {
            String b = blocked.trim().toLowerCase().replace("/", "");
            if (b.isEmpty()) {
                continue;
            }
            if (command.equalsIgnoreCase(b) || command.toLowerCase().startsWith(b.toLowerCase() + ":")) {
                if (plugin.getCommandCooldown().isBlocked(player)) {
                    long seconds = plugin.getCommandCooldown().remainingSeconds(player);
                    String msg = cfg.getBlockMessage().replace("%seconds%", String.valueOf(seconds));
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                    event.setCancelled(true);
                }
                return; // 只匹配被禁指令，匹配到后无论是否过期都结束
            }
        }
    }
}