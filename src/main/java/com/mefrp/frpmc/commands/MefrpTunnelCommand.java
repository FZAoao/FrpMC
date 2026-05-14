package com.mefrp.frpmc.commands;

import com.mefrp.frpmc.Main;
import com.mefrp.frpmc.api.MefrpApiClient;
import com.mefrp.frpmc.util.AsyncTask;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class MefrpTunnelCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public MefrpTunnelCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("frpmc.command")) {
            sender.sendMessage(ChatColor.RED + " You don't have permission to use this command.");
            return true;
        }

        if (!plugin.getApiClient().hasToken()) {
            sender.sendMessage(ChatColor.RED + " Please login first using /mefrp login");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "list":
                listTunnels(sender);
                break;
            case "create":
                if (sender instanceof Player) {
                    createTunnel(sender, args);
                } else {
                    sender.sendMessage(ChatColor.RED + " This command must be used in-game.");
                }
                break;
            case "delete":
                deleteTunnel(sender, args);
                break;
            case "start":
                startTunnel(sender, args);
                break;
            case "stop":
                stopTunnel(sender, args);
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + " === Tunnel Management ===");
        sender.sendMessage(ChatColor.YELLOW + "/mefrp tunnel list" + ChatColor.WHITE + " - List all tunnels");
        sender.sendMessage(ChatColor.YELLOW + "/mefrp tunnel create <name> <node> <localPort> <remotePort> <type>" + ChatColor.WHITE + " - Create a tunnel");
        sender.sendMessage(ChatColor.YELLOW + "/mefrp tunnel delete <id>" + ChatColor.WHITE + " - Delete a tunnel");
        sender.sendMessage(ChatColor.YELLOW + "/mefrp tunnel start <id>" + ChatColor.WHITE + " - Start a tunnel");
        sender.sendMessage(ChatColor.YELLOW + "/mefrp tunnel stop <id>" + ChatColor.WHITE + " - Stop a tunnel");
        sender.sendMessage(ChatColor.GRAY + " Tunnel types: tcp, udp, http, https");
    }

    private void listTunnels(CommandSender sender) {
        AsyncTask.executeAsync(plugin, sender,
            () -> {
                MefrpApiClient.ApiResponse response = plugin.getApiClient().listTunnels();
                if (response.isSuccess()) {
                    return AsyncTask.AsyncResult.success("=== Your Tunnels ===", response.getString("data"));
                } else {
                    return AsyncTask.AsyncResult.error("Failed to load tunnels: " + response.getMessage());
                }
            },
            ChatColor.YELLOW + " Loading tunnels...");
    }

    private void createTunnel(CommandSender sender, String[] args) {
        if (args.length < 6) {
            sender.sendMessage(ChatColor.RED + " Usage: /mefrp tunnel create <name> <nodeId> <localPort> <remotePort> <type>");
            return;
        }

        String name = args[1];
        String nodeId = args[2];
        int localPort;
        try {
            localPort = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + " Invalid local port number.");
            return;
        }
        
        String remotePort = args[4];
        String tunnelType = args[5].toLowerCase();

        sender.sendMessage(ChatColor.YELLOW + " Creating tunnel...");

        AsyncTask.executeAsync(plugin, sender,
            () -> {
                MefrpApiClient.ApiResponse response = plugin.getApiClient().createTunnel(
                        name, nodeId, "127.0.0.1", localPort, remotePort, tunnelType
                );
                if (response.isSuccess()) {
                    return AsyncTask.AsyncResult.success("Tunnel created successfully!");
                } else {
                    return AsyncTask.AsyncResult.error("Failed to create tunnel: " + response.getMessage());
                }
            },
            null);
    }

    private void deleteTunnel(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + " Usage: /mefrp tunnel delete <tunnelId>");
            return;
        }

        String tunnelId = args[1];
        sender.sendMessage(ChatColor.YELLOW + " Deleting tunnel...");

        AsyncTask.executeAsync(plugin, sender,
            () -> {
                MefrpApiClient.ApiResponse response = plugin.getApiClient().deleteTunnel(tunnelId);
                if (response.isSuccess()) {
                    return AsyncTask.AsyncResult.success("Tunnel deleted successfully!");
                } else {
                    return AsyncTask.AsyncResult.error("Failed to delete tunnel: " + response.getMessage());
                }
            },
            null);
    }

    private void startTunnel(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + " Usage: /mefrp tunnel start <tunnelId>");
            return;
        }

        String tunnelId = args[1];
        sender.sendMessage(ChatColor.YELLOW + " Starting tunnel...");

        AsyncTask.executeAsync(plugin, sender,
            () -> {
                MefrpApiClient.ApiResponse response = plugin.getApiClient().startTunnel(tunnelId);
                if (response.isSuccess()) {
                    return AsyncTask.AsyncResult.success("Tunnel started successfully!");
                } else {
                    return AsyncTask.AsyncResult.error("Failed to start tunnel: " + response.getMessage());
                }
            },
            null);
    }

    private void stopTunnel(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + " Usage: /mefrp tunnel stop <tunnelId>");
            return;
        }

        String tunnelId = args[1];
        sender.sendMessage(ChatColor.YELLOW + " Stopping tunnel...");

        AsyncTask.executeAsync(plugin, sender,
            () -> {
                MefrpApiClient.ApiResponse response = plugin.getApiClient().stopTunnel(tunnelId);
                if (response.isSuccess()) {
                    return AsyncTask.AsyncResult.success("Tunnel stopped successfully!");
                } else {
                    return AsyncTask.AsyncResult.error("Failed to stop tunnel: " + response.getMessage());
                }
            },
            null);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("list");
            completions.add("create");
            completions.add("delete");
            completions.add("start");
            completions.add("stop");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            completions.add("tcp");
            completions.add("udp");
            completions.add("http");
            completions.add("https");
        }

        return completions;
    }
}