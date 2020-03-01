package net.azisaba.main.nintendoswitch;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import net.azisaba.main.nintendoswitch.enums.SwitchMode;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.DefaultContextKeys;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeBuilder;

public class PermManager {

    private static NintendoSwitch plugin;

    public static void init(NintendoSwitch plugin) {
        PermManager.plugin = plugin;
    }

    public static boolean setGroup(Player p, String name) {

        LuckPerms api = LuckPermsProvider.get();
        User user = api.getUserManager().getUser(p.getUniqueId());

        List<Node> groupList = user.getNodes().stream()
                .filter(n -> n.getKey().startsWith("group."))
                .collect(Collectors.toList());

        groupList.forEach(node -> {
            if ( !node.getContexts().containsKey(DefaultContextKeys.SERVER_KEY) ) {
                return;
            }

            if ( !node.getContexts().getValues(DefaultContextKeys.SERVER_KEY).contains(plugin.getPluginConfig().serverName) ) {
                return;
            }

            removeNode(p, node);

        });

        Node newNode = Node.builder("group." + name)
                .withContext(DefaultContextKeys.SERVER_KEY, "main")
                .build();
        addNode(p, newNode);

        return true;
    }

    public static void setModePermission(Player p, SwitchMode mode) {
        List<Node> nodeList = new ArrayList<>(getGroupNodes(p));

        if ( mode == SwitchMode.DEFAULT ) {
            for ( Node node : nodeList ) {

                if ( node.getContexts().containsKey(DefaultContextKeys.SERVER_KEY) ) {
                    if ( !node.getContexts().getValues(DefaultContextKeys.SERVER_KEY).contains(plugin.getPluginConfig().serverName) ) {
                        continue;
                    }
                }

                removeNode(p, node);
            }

            Node nodeNormal = Node.builder("group." + plugin.getPluginConfig().getMemberGroupName(p))
                    .withContext(DefaultContextKeys.SERVER_KEY, plugin.getPluginConfig().serverName)
                    .build();
            Node nodeRank10 = Node.builder("group.rank10")
                    .withContext(DefaultContextKeys.SERVER_KEY, plugin.getPluginConfig().serverName)
                    .build();

            addNode(p, nodeNormal);
            addNode(p, nodeRank10);
        } else if ( mode == SwitchMode.ADMIN ) {

            for ( Node node : nodeList ) {

                if ( node.getContexts().containsKey(DefaultContextKeys.SERVER_KEY) ) {
                    if ( node.getContexts().getValues(DefaultContextKeys.SERVER_KEY).contains(plugin.getPluginConfig().serverName) ) {
                        removeNode(p, node);
                    }
                }
            }

            String perm = plugin.getPluginConfig().getAdminGroupName(p);
            String[] splitPerm = perm.split(":");

            String server = null;

            if ( splitPerm.length > 1 ) {
                server = splitPerm[0];
                perm = splitPerm[1];
            }

            @SuppressWarnings("rawtypes")
            NodeBuilder builder = Node.builder("group." + perm);

            if ( server != null ) {
                builder.withContext(DefaultContextKeys.SERVER_KEY, server);
            }

            addNode(p, builder.build());

            Node defaultNode = Node.builder("group.default").build();
            removeNode(p, defaultNode);
        }
    }

    public static boolean addNode(Player p, Node node) {
        LuckPerms api = LuckPermsProvider.get();
        User user = api.getUserManager().getUser(p.getUniqueId());

        DataMutateResult result = user.data().add(node);
        if ( result.wasSuccessful() ) {
            api.getUserManager().saveUser(user);
            return true;
        } else {
            return false;
        }
    }

    public static boolean removeNode(Player p, Node node) {
        if ( node.getKey().equalsIgnoreCase("group.switcher") ) {
            return true;
        }

        LuckPerms api = LuckPermsProvider.get();
        User user = api.getUserManager().getUser(p.getUniqueId());

        DataMutateResult result = user.data().remove(node);
        if ( result.wasSuccessful() ) {
            api.getUserManager().saveUser(user);
            return true;
        } else {
            return false;
        }
    }

    public static List<Node> getGroupNodes(Player p) {
        LuckPerms api = LuckPermsProvider.get();
        User user = api.getUserManager().getUser(p.getUniqueId());

        return user.getNodes().stream()
                .filter(node -> node.getKey().startsWith("group."))
                .collect(Collectors.toList());
    }
}
