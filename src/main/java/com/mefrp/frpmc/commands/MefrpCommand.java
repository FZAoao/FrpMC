package com.mefrp.frpmc.commands;

import com.mefrp.frpmc.Main;
import com.mefrp.frpmc.api.MefrpApiClient;
import com.mefrp.frpmc.util.AsyncTask;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import java.util.ArrayList;
import java.util.List;

public class MefrpCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public MefrpCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("frpmc.command")) {
            sender.sendMessage(ChatColor.RED + " You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                sendHelp(sender);
                break;
            case "status":
                plugin.getCommand("mefrp-status").execute(sender, command.getLabel(), args);
                break;
            case "login":
                plugin.getCommand("mefrp-login").execute(sender, command.getLabel(), args);
                break;
            case "tunnel":
                plugin.getCommand("mefrp-tunnel").execute(sender, command.getLabel(), args);
                break;
            case "nodes":
                showNodes(sender);
                break;
            case "reload":
                reloadPlugin(sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + " Unknown subcommand. Use /mefrp help");
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + " === Mefrp FRP Plugin Help ===");
        sender.sendMessage(ChatColor.YELLOW + "/mefrp help" + ChatColor.WHITE + " - Show this help message");
        sender.sendMessage(ChatColor.YELLOW + "/mefrp login <user> <pass>" + ChatColor.WHITE + " - Login to Mefrp API");
        sender.sendMessage(ChatColor.YELLOW + "/mefrp logout" + ChatColor.WHITE + " - Logout from Mefrp API");
        sender.sendMessage(ChatColor.YELLOW + "/mefrp status" + ChatColor.WHITE + " - Show connection status");
        sender.sendMessage(ChatColor.YELLOW + "/mefrp tunnel" + ChatColor.WHITE + " - Manage tunnels (create, list, delete, start, stop)");
        sender.sendMessage(ChatColor.YELLOW + "/mefrp nodes" + ChatColor.WHITE + " - List available nodes");
        sender.sendMessage(ChatColor.YELLOW + "/mefrp reload" + ChatColor.WHITE + " - Reload config and messages (admin)");
    }

    private void showNodes(CommandSender sender) {
        if (!plugin.getApiClient().hasToken()) {
            sender.sendMessage(ChatColor.RED + " Please login first using /mefrp login");
            return;
        }

        AsyncTask.executeAsync(plugin, sender,
            () -> {
                MefrpApiClient.ApiResponse response = plugin.getApiClient().getNodeList();
                if (response.isSuccess()) {
                    return AsyncTask.AsyncResult.success("Nodes available:", response.getString("data"));
                } else {
                    return AsyncTask.AsyncResult.error("Failed to get nodes: " + response.getMessage());
                }
            },
            ChatColor.GOLD + " Loading available nodes...");
    }

    private void logout(CommandSender sender) {
        plugin.getApiClient().disconnect();
        sender.sendMessage(ChatColor.GREEN + " You have been logged out from Mefrp API.");
    }
    
    private void reloadPlugin(CommandSender sender) {
        if (!sender.hasPermission("frpmc.admin")) {
            sender.sendMessage(ChatColor.RED + " You don't have permission to reload the plugin.");
            return;
        }
        
        sender.sendMessage(ChatColor.YELLOW + " Reloading FrpMC configuration...");
        
        if (plugin.reloadPlugin()) {
            sender.sendMessage(ChatColor.GREEN + " FrpMC reloaded successfully!");
        } else {
            sender.sendMessage(ChatColor.RED + " Failed to reload FrpMC. Check console for details.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("help");
            completions.add("login");
            completions.add("logout");
            completions.add("status");
            completions.add("tunnel");
            completions.add("nodes");
            completions.add("reload");
        }

        return completions;
    }
}