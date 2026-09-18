package com.deathdrop;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * 主插件类
 */
public final class DeathDropPlugin extends JavaPlugin {

    private PluginConfig pluginConfig;
    private DeathTracker deathTracker;
    private CommandCooldown commandCooldown;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.pluginConfig = new PluginConfig(this);
        this.deathTracker = new DeathTracker(this);
        this.commandCooldown = new CommandCooldown(this);

        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandListener(this), this);

        getCommand("deathdrop").setExecutor(new DeathDropCommand(this));

        getLogger().info("DeathDrop 已启用");
    }

    @Override
    public void onDisable() {
        getLogger().info("DeathDrop 已关闭");
    }

    public void reload() {
        reloadConfig();
        pluginConfig.reload();
        deathTracker.clear();
        commandCooldown.clear();
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public DeathTracker getDeathTracker() {
        return deathTracker;
    }

    public CommandCooldown getCommandCooldown() {
        return commandCooldown;
    }
}