package com.deathdrop;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * /deathdrop 主指令
 */
public class DeathDropCommand implements CommandExecutor {

    private final DeathDropPlugin plugin;

    public DeathDropCommand(DeathDropPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reload();
            sender.sendMessage(ChatColor.GREEN + "DeathDrop 配置已重载。");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Usage: /deathdrop reload");
        return true;
    }
}