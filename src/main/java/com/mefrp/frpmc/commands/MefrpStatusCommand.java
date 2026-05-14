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

public class MefrpStatusCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public MefrpStatusCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("frpmc.command")) {
            sender.sendMessage(ChatColor.RED + " You don't have permission to use this command.");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + " === Mefrp Status ===");

        // Check connection status
        if (plugin.getApiClient().isConnected()) {
            sender.sendMessage(ChatColor.GREEN + " Status: Connected");
            sender.sendMessage(ChatColor.GREEN + " Token: Present");

            // Get user info
            sender.sendMessage(ChatColor.YELLOW + " Loading user info...");

            AsyncTask.executeAsync(plugin, sender,
                () -> {
                    MefrpApiClient.ApiResponse response = plugin.getApiClient().getUserInfo();
                    if (response.isSuccess()) {
                        String username = response.getString("username");
                        String email = response.getString("email");
                        StringBuilder sb = new StringBuilder();
                        sb.append(ChatColor.GREEN).append(" Username: ").append(ChatColor.WHITE).append(username);
                        if (!email.isEmpty()) {
                            sb.append("\n").append(ChatColor.GREEN).append(" Email: ").append(ChatColor.WHITE).append(email);
                        }
                        return AsyncTask.AsyncResult.success("User info loaded", sb.toString());
                    } else {
                        return AsyncTask.AsyncResult.error("Failed to get user info: " + response.getMessage());
                    }
                },
                null);

        } else if (plugin.getApiClient().hasToken()) {
            sender.sendMessage(ChatColor.YELLOW + " Status: Token present but not verified");
            sender.sendMessage(ChatColor.YELLOW + " Use /mefrp login to reconnect");
        } else {
            sender.sendMessage(ChatColor.RED + " Status: Not logged in");
            sender.sendMessage(ChatColor.YELLOW + " Use /mefrp login <username> <password> to login");
        }

        // Show system stats
        sender.sendMessage(ChatColor.GOLD + " === Public Stats ===");

        AsyncTask.executeAsync(plugin, sender,
            () -> {
                MefrpApiClient.ApiResponse response = plugin.getApiClient().getPublicStats();
                if (response.isSuccess()) {
                    return AsyncTask.AsyncResult.success("Stats:", response.getString("data"), ChatColor.WHITE);
                } else {
                    return AsyncTask.AsyncResult.success("Public stats unavailable", null, ChatColor.GRAY);
                }
            },
            null);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}