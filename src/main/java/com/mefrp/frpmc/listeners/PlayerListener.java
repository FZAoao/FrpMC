package com.mefrp.frpmc.listeners;

import com.mefrp.frpmc.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final Main plugin;

    public PlayerListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Check if auto-reconnect is enabled and we have a token
        if (plugin.getConfig().getBoolean("auto-reconnect.enabled", true)) {
            if (plugin.getApiClient().hasToken() && !plugin.getApiClient().isConnected()) {
                if (player.hasPermission("frpmc.admin")) {
                    player.sendMessage(ChatColor.YELLOW + "[FrpMC] Attempting to reconnect to Mefrp API...");
                    // Attempt reconnect would happen here
                }
            }
        }

        // Show status notification if enabled
        if (plugin.getConfig().getBoolean("status.show-in-chat", true)) {
            if (player.hasPermission("frpmc.admin")) {
                if (plugin.getApiClient().isConnected()) {
                    player.sendMessage(ChatColor.GREEN + "[FrpMC] Connected to Mefrp API");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Any cleanup or notification logic can go here
    }
}