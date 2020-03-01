package net.azisaba.main.nintendoswitch.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.azisaba.main.nintendoswitch.SwitchManager;

public class JoinLeftListener implements Listener {

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        if ( e.getPlayer().hasPermission("nintendoswitch.command.switch") ) {
            SwitchManager.registerSwitchPlayer(e.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        SwitchManager.clearPlayer(e.getPlayer());
    }
}
