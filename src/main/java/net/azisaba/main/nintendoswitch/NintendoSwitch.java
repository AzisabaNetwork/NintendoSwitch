package net.azisaba.main.nintendoswitch;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import net.azisaba.main.nintendoswitch.commands.SwitchAdminCommand;
import net.azisaba.main.nintendoswitch.commands.SwitchCommand;
import net.azisaba.main.nintendoswitch.listener.JoinLeftListener;

import lombok.Getter;

public class NintendoSwitch extends JavaPlugin {

    @Getter
    private NintendoSwitchConfig pluginConfig;

    @Override
    public void onEnable() {

        pluginConfig = new NintendoSwitchConfig(this);
        pluginConfig.loadConfig();

        Bukkit.getPluginManager().registerEvents(new JoinLeftListener(), this);

        Bukkit.getPluginCommand("switch").setExecutor(new SwitchCommand());
        Bukkit.getPluginCommand("switchadmin").setExecutor(new SwitchAdminCommand(this));

        SwitchManager.init(this);
        PermManager.init(this);

        Bukkit.getOnlinePlayers().forEach(p -> {
            if ( p.hasPermission("nintendoswitch.command.switch") ) {
                SwitchManager.registerSwitchPlayer(p);
            }
        });

        Bukkit.getLogger().info(getName() + " enabled.");
    }

    @Override
    public void onDisable() {
        for ( SwitchPlayer p : SwitchManager.getAllSwitchPlayers() ) {
            p.saveAs(p.getCurrentMode());
            p.returnPermissionNode();
        }

        Bukkit.getLogger().info(getName() + " disabled.");
    }

    @Override
    public void reloadConfig() {
        pluginConfig = new NintendoSwitchConfig(this);
        pluginConfig.loadConfig();
    }
}
