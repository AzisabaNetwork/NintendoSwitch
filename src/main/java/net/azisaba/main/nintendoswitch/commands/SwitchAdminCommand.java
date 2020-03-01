package net.azisaba.main.nintendoswitch.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.azisaba.main.nintendoswitch.NintendoSwitch;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SwitchAdminCommand implements CommandExecutor {

    private final NintendoSwitch plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if ( args.length <= 0 ) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " reload");
            return true;
        }
        if ( args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl") ) {
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "Configをリロードしました！");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Usage: /" + label + " reload");
        return true;
    }
}
