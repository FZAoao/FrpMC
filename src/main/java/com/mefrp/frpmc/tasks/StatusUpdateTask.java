package com.mefrp.frpmc.tasks;

import com.mefrp.frpmc.Main;
import com.mefrp.frpmc.api.MefrpApiClient;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Periodic task to check and maintain API connection status.
 * Handles automatic reconnection attempts.
 */
public class StatusUpdateTask extends BukkitRunnable {

    private final Main plugin;
    private int consecutiveFailures = 0;

    public StatusUpdateTask(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // Skip if no token exists
        if (!plugin.getApiClient().hasToken()) {
            consecutiveFailures = 0;
            return;
        }

        // Check connection status
        if (!plugin.getApiClient().isConnected()) {
            int maxRetries = plugin.getConfig().getInt("auto-reconnect.max-retries", 5);

            if (consecutiveFailures < maxRetries) {
                consecutiveFailures++;
                
                // Try to verify token by getting user info
                MefrpApiClient.ApiResponse response = plugin.getApiClient().getUserInfo();
                
                if (response.isSuccess()) {
                    // Token is still valid, mark as connected
                    plugin.getApiClient().setToken(plugin.getApiClient().getToken());
                    consecutiveFailures = 0;
                    plugin.getLogger().info("Reconnected to Mefrp API successfully.");
                } else {
                    plugin.getLogger().warning("Reconnection attempt " + consecutiveFailures + " failed: " + response.getMessage());
                }
            } else {
                plugin.getLogger().severe("Failed to reconnect after " + maxRetries + " attempts. Token may be expired.");
                consecutiveFailures = 0;
            }
        } else {
            consecutiveFailures = 0;
        }
    }

    /**
     * Reset retry counter (useful for manual reconnect attempts)
     */
    public void resetRetries() {
        this.consecutiveFailures = 0;
    }
}