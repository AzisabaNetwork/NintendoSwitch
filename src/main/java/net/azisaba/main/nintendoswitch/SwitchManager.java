package net.azisaba.main.nintendoswitch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;

import net.azisaba.main.nintendoswitch.enums.SwitchMode;

public class SwitchManager {

    private static HashMap<Player, SwitchPlayer> pMap = new HashMap<>();

    private static NintendoSwitch plugin;

    public static void init(NintendoSwitch plugin) {
        SwitchManager.plugin = plugin;
    }

    public static void switchPlayerMode(Player p) {
        if ( !pMap.containsKey(p) ) {
            SwitchPlayer sp = new SwitchPlayer(plugin, p);
            sp.toggleMode();

            pMap.put(p, sp);
        } else {
            pMap.get(p).toggleMode();
        }
    }

    public static boolean switchPlayerMode(Player p, SwitchMode mode) {
        if ( !pMap.containsKey(p) ) {
            SwitchPlayer sp = new SwitchPlayer(plugin, p);
            sp.setMode(mode);

            pMap.put(p, sp);
            return true;
        } else {
            return pMap.get(p).setMode(mode);
        }
    }

    public static void clearPlayer(Player p) {
        if ( pMap.containsKey(p) ) {
            pMap.get(p).returnPermissionNode();
            pMap.remove(p);
        }
    }

    public static void clearAllPlayers() {

        if ( pMap.keySet() == null ) {
            return;
        }

        for ( Player p : pMap.keySet() ) {
            pMap.get(p).returnPermissionNode();
        }

        pMap.clear();
    }

    public static void registerSwitchPlayer(Player p) {
        if ( !pMap.containsKey(p) ) {
            SwitchPlayer sp = new SwitchPlayer(plugin, p);
            pMap.put(p, sp);
        }
    }

    public static SwitchPlayer getSwitchPlayer(Player p) {
        if ( pMap.containsKey(p) ) {
            return pMap.get(p);
        }

        return null;
    }

    public static List<SwitchPlayer> getAllSwitchPlayers() {
        return new ArrayList<>(pMap.values());
    }
}
