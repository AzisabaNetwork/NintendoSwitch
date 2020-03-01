package net.azisaba.main.nintendoswitch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.azisaba.main.nintendoswitch.enums.SwitchMode;
import net.luckperms.api.context.DefaultContextKeys;
import net.luckperms.api.node.Node;
import net.md_5.bungee.api.ChatColor;

public class SwitchPlayer {

    private NintendoSwitch plugin;
    private Player player;

    private List<Node> defaultNodes;

    private SwitchMode currentMode = SwitchMode.DEFAULT;

    public SwitchPlayer(NintendoSwitch plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        defaultNodes = PermManager.getGroupNodes(player);

        for ( Node node : new ArrayList<>(defaultNodes) ) {
            PermManager.removeNode(player, node);
        }

        File file = new File(new File(plugin.getDataFolder(), "PlayerData"),
                player.getUniqueId().toString().replace("-", "") + ".yml");

        if ( !file.exists() ) {
            currentMode = SwitchMode.DEFAULT;
            PermManager.setModePermission(player, currentMode);
            return;
        }

        YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
        currentMode = SwitchMode.valueOf(conf.getString("CurrentMode", "DEFAULT"));

        PermManager.setModePermission(player, currentMode);
    }

    public boolean setMode(SwitchMode mode) {

        if ( currentMode == mode ) {
            player.sendMessage(ChatColor.RED + "すでにそのモードです。");
            return false;
        }

        if ( mode == SwitchMode.ADMIN ) {
            setAdmin();
        } else {
            setDefault();
        }

        return true;
    }

    public void toggleMode() {
        if ( currentMode == SwitchMode.DEFAULT ) {
            setMode(SwitchMode.ADMIN);
        } else {
            setMode(SwitchMode.DEFAULT);
        }
    }

    public void returnPermissionNode() {
        List<Node> nodeList = PermManager.getGroupNodes(player);

        for ( Node node : defaultNodes ) {
            PermManager.addNode(player, node);
        }

        for ( Node node : nodeList ) {
            List<String> serverList = new ArrayList<>();
            if ( node.getContexts().containsKey(DefaultContextKeys.SERVER_KEY) ) {
                serverList.addAll(node.getContexts().getValues(DefaultContextKeys.SERVER_KEY));
            }

            if ( serverList.contains(plugin.getPluginConfig().serverName)
                    && !node.getKey().toLowerCase().startsWith("group.rank") ) {
                PermManager.removeNode(player, node);
            }
        }

        Node nodeRank10 = Node.builder("group.rank10")
                .withContext(DefaultContextKeys.SERVER_KEY, plugin.getPluginConfig().serverName)
                .build();
        PermManager.addNode(player, nodeRank10);

        nodeList = PermManager.getGroupNodes(player);

        for ( Node node : nodeList ) {
            if ( node.getKey().equals("group.default") ) {
                PermManager.removeNode(player, node);
            }
        }
    }

    public SwitchMode getCurrentMode() {
        return currentMode;
    }

    private void setAdmin() {
        saveAs(SwitchMode.DEFAULT);
        load(SwitchMode.ADMIN);

        PermManager.setModePermission(player, SwitchMode.ADMIN);

        player.setGameMode(GameMode.CREATIVE);
        player.sendMessage(ChatColor.YELLOW + "モードを" + ChatColor.RED + "Admin" + ChatColor.YELLOW + "に変更しました");

        currentMode = SwitchMode.ADMIN;
    }

    private void setDefault() {
        saveAs(SwitchMode.ADMIN);
        load(SwitchMode.DEFAULT);

        player.performCommand("essentials:vanish disable");

        PermManager.setModePermission(player, SwitchMode.DEFAULT);

        player.setGameMode(GameMode.SURVIVAL);
        player.sendMessage(ChatColor.YELLOW + "モードを" + ChatColor.GRAY + "Default" + ChatColor.YELLOW + "に変更しました");

        currentMode = SwitchMode.DEFAULT;
    }

    public boolean saveAs(SwitchMode mode) {

        File file = new File(new File(plugin.getDataFolder(), "PlayerData"),
                player.getUniqueId().toString().replace("-", "") + ".yml");

        if ( !file.exists() ) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch ( IOException e ) {
                e.printStackTrace();
                return false;
            }
        }

        YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);

        List<ItemStack> contents = Arrays.asList(player.getInventory().getContents());
        List<ItemStack> armors = Arrays.asList(player.getInventory().getArmorContents());
        ItemStack offHand = player.getInventory().getItemInOffHand();

        if ( offHand != null && offHand.getType() == Material.AIR ) {
            offHand = null;
        }

        int num = -1;
        for ( ItemStack item : contents ) {
            num++;
            conf.set(mode.toString() + ".Inventory.Contents." + num, item);
        }

        num = -1;
        for ( ItemStack item : armors ) {
            num++;
            conf.set(mode.toString() + ".Inventory.Armors." + num, item);
        }

        conf.set(mode.toString() + ".Inventory.OffHand", offHand);
        conf.set(mode.toString() + ".Location", convertToString(player.getLocation()));

        conf.set(mode.toString() + ".HP", player.getHealth());
        conf.set(mode.toString() + ".Food", player.getFoodLevel());

        try {
            conf.save(file);
        } catch ( IOException e ) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean load(SwitchMode mode) {

        File file = new File(new File(plugin.getDataFolder(), "PlayerData"),
                player.getUniqueId().toString().replace("-", "") + ".yml");

        if ( !file.exists() ) {
            return false;
        }

        YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);

        ItemStack[] contents = new ItemStack[36];
        ItemStack[] armors = new ItemStack[4];
        ItemStack offhand = new ItemStack(Material.AIR);
        Location location = null;

        for ( int i = 0; i <= 35; i++ ) {
            if ( conf.get(mode.toString() + ".Inventory.Contents." + i) != null ) {
                contents[i] = conf.getItemStack(mode.toString() + ".Inventory.Contents." + i);
            }
        }

        for ( int i = 0; i <= 3; i++ ) {
            if ( conf.get(mode.toString() + ".Inventory.Armors." + i) != null ) {
                armors[i] = conf.getItemStack(mode.toString() + ".Inventory.Armors." + i);
            }
        }

        if ( conf.get(mode.toString() + ".Inventory.OffHand") != null ) {
            offhand = conf.getItemStack(mode.toString() + ".Inventory.OffHand");
        }

        if ( conf.get(mode.toString() + ".Location") != null ) {
            location = parseLocation(conf.getString(mode.toString() + ".Location"));
        }

        player.getInventory().setContents(contents);
        player.getInventory().setArmorContents(armors);
        player.getInventory().setItemInOffHand(offhand);

        if ( location != null && location.getWorld() != null ) {
            player.teleport(location);
        }

        double health = conf.getDouble(mode.toString() + ".HP", player.getHealth());
        int food = conf.getInt(mode.toString() + ".Food", player.getFoodLevel());

        player.setHealth(health);
        player.setFoodLevel(food);

        conf.set("CurrentMode", mode.toString());
        try {
            conf.save(file);
        } catch ( IOException e ) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private String convertToString(Location loc) {
        if ( loc == null ) {
            return null;
        }

        String world = loc.getWorld().getName();
        if ( loc.getWorld() == null ) {
            world = "";
        }

        return world + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getPitch() + ","
                + loc.getYaw();
    }

    private Location parseLocation(String str) {
        String[] strs = str.split(",");

        if ( strs.length <= 1 ) {
            return null;
        }

        Location loc = null;
        try {
            loc = new Location(Bukkit.getWorld(strs[0]), Double.parseDouble(strs[1]), Double.parseDouble(strs[2]),
                    Double.parseDouble(strs[3]));

            loc.setPitch(Float.parseFloat(strs[4]));
            loc.setYaw(Float.parseFloat(strs[5]));
        } catch ( Exception e ) {
            return loc;
        }

        return loc;
    }
}
