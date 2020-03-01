package net.azisaba.main.nintendoswitch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class NintendoSwitchConfig {

    private final NintendoSwitch plugin;
    private final FileConfiguration conf;

    @ConfigOptions(path = "Server.ServerName")
    public String serverName = "main";

    @ConfigOptions(path = "Permission.Admin.Group")
    public String adminGroup = "admin";
    @ConfigOptions(path = "Permission.Member.Group")
    public String defaultGroup = "member";

    private final HashMap<UUID, String> adminMap = new HashMap<>();
    private final HashMap<UUID, String> memberMap = new HashMap<>();

    public NintendoSwitchConfig(NintendoSwitch plugin) {
        this.plugin = plugin;
        conf = plugin.getConfig();
    }

    public void loadConfig() {
        for ( Field field : getClass().getFields() ) {
            ConfigOptions anno = field.getAnnotation(ConfigOptions.class);

            if ( anno == null ) {
                continue;
            }

            String path = anno.path();

            if ( conf.get(path) == null ) {

                try {

                    if ( anno.type() == OptionType.NONE ) {
                        conf.set(path, field.get(this));
                    } else if ( anno.type() == OptionType.LOCATION ) {
                        Location loc = (Location) field.get(this);

                        conf.set(path, loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ()
                                + "," + loc.getYaw() + "," + loc.getPitch());
                    } else if ( anno.type() == OptionType.CHAT_FORMAT ) {

                        String msg = (String) field.get(this);
                        conf.set(path, msg);

                        msg = msg.replace("&", "§");
                        field.set(this, msg);
                    } else if ( anno.type() == OptionType.SOUND ) {
                        conf.set(path, field.get(this).toString());
                    } else if ( anno.type() == OptionType.LOCATION_LIST ) {
                        @SuppressWarnings("unchecked")
                        List<Location> locations = (List<Location>) field.get(this);

                        List<String> strs = new ArrayList<>();

                        if ( !locations.isEmpty() ) {

                            for ( Location loc : locations ) {
                                strs.add(loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + ","
                                        + loc.getZ()
                                        + "," + loc.getYaw() + "," + loc.getPitch());
                            }
                        } else {
                            strs.add("WorldName,X,Y,Z,Yaw,Pitch");
                        }

                        conf.set(path, strs);
                    }

                    plugin.saveConfig();
                } catch ( Exception e ) {
                    Bukkit.getLogger().warning("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {

                try {
                    if ( anno.type() == OptionType.NONE ) {
                        field.set(this, conf.get(path));
                    } else if ( anno.type() == OptionType.LOCATION ) {

                        String[] strings = conf.getString(path).split(",");
                        Location loc = null;
                        try {
                            loc = new Location(Bukkit.getWorld(strings[0]), Double.parseDouble(strings[1]),
                                    Double.parseDouble(strings[2]), Double.parseDouble(strings[3]));
                            loc.setYaw(Float.parseFloat(strings[4]));
                            loc.setPitch(Float.parseFloat(strings[5]));
                        } catch ( Exception e ) {
                            // None
                        }

                        if ( loc == null ) {
                            Bukkit.getLogger().warning("Error. " + path + " の値がロードできませんでした。");
                            continue;
                        }

                        field.set(this, loc);
                    } else if ( anno.type() == OptionType.SOUND ) {

                        String name = conf.getString(path);
                        Sound sound;

                        try {
                            sound = Sound.valueOf(name.toUpperCase());
                        } catch ( Exception e ) {
                            Bukkit.getLogger().warning("Error. " + path + " の値がロードできませんでした。");
                            continue;
                        }

                        field.set(this, sound);
                    } else if ( anno.type() == OptionType.CHAT_FORMAT ) {

                        String unformatMessage = conf.getString(path);

                        unformatMessage = unformatMessage.replace("&", "§");

                        field.set(this, unformatMessage);
                    } else if ( anno.type() == OptionType.LOCATION_LIST ) {

                        List<String> strList = conf.getStringList(path);

                        List<Location> locList = new ArrayList<>();

                        for ( String str : strList ) {

                            String[] strings = str.split(",");
                            Location loc = null;
                            try {
                                loc = new Location(Bukkit.getWorld(strings[0]), Double.parseDouble(strings[1]),
                                        Double.parseDouble(strings[2]), Double.parseDouble(strings[3]));
                                loc.setYaw(Float.parseFloat(strings[4]));
                                loc.setPitch(Float.parseFloat(strings[5]));
                            } catch ( Exception e ) {
                                // None
                            }

                            if ( loc == null ) {
                                Bukkit.getLogger().warning("Error. " + path + " の " + str + "がロードできませんでした。");
                                continue;
                            }

                            locList.add(loc);
                        }

                        field.set(this, locList);
                    }
                } catch ( Exception e ) {
                    Bukkit.getLogger().warning("Error. " + e.getMessage());
                }
            }
        }

        boolean ret = false;

        if ( conf.getConfigurationSection("Additional.Member") == null ) {
            conf.set("Additional.Member.7daf21e7-b275-43dd-bc0d-4762c73d6199", "vip");
            ret = true;
        }
        if ( conf.getConfigurationSection("Additional.Admin") == null ) {
            conf.set("Additional.Admin.7daf21e7-b275-43dd-bc0d-4762c73d6199", "developer");
            ret = true;
        }

        if ( ret ) {
            plugin.saveConfig();
            return;
        }

        conf.getConfigurationSection("Additional.Admin").getKeys(false).forEach(str -> {

            String path = "Additional.Admin." + str;

            String group = conf.getString(path);

            adminMap.put(UUID.fromString(str), group);
        });

        conf.getConfigurationSection("Additional.Member").getKeys(false).forEach(str -> {

            String path = "Additional.Member." + str;

            String group = conf.getString(path);

            memberMap.put(UUID.fromString(str), group);
        });
    }

    public String getAdminGroupName(Player p) {
        if ( adminMap.containsKey(p.getUniqueId()) ) {
            return adminMap.get(p.getUniqueId());
        }

        return adminGroup;
    }

    public String getMemberGroupName(Player p) {
        if ( memberMap.containsKey(p.getUniqueId()) ) {
            return memberMap.get(p.getUniqueId());
        }

        return defaultGroup;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ConfigOptions {
        public String path();

        public OptionType type() default OptionType.NONE;
    }

    public enum OptionType {
        LOCATION,
        LOCATION_LIST,
        SOUND,
        CHAT_FORMAT,
        NONE
    }
}
