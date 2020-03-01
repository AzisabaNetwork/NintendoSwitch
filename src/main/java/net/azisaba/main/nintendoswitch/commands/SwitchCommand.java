package net.azisaba.main.nintendoswitch.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.azisaba.main.nintendoswitch.SwitchManager;
import net.azisaba.main.nintendoswitch.enums.SwitchMode;
import net.md_5.bungee.api.ChatColor;

public class SwitchCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if ( !(sender instanceof Player) ) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ有効です！");
            return true;
        }

        Player p = (Player) sender;

        if ( args.length == 0 ) {
            SwitchManager.switchPlayerMode(p);
        } else if ( args.length >= 1 ) {
            String modeStr = args[0].toUpperCase();
            SwitchMode mode;

            try {
                mode = SwitchMode.valueOf(modeStr);
            } catch ( Exception e ) {
                p.sendMessage(ChatColor.RED + "正しいモードを指定してください(" + ChatColor.YELLOW + "admin" + ChatColor.RED + "/"
                        + ChatColor.YELLOW + "default" + ChatColor.RED + ")");
                return true;
            }

            SwitchManager.switchPlayerMode(p, mode);
        }
        return true;
    }
}
