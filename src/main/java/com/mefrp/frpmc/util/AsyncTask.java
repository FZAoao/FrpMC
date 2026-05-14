package com.mefrp.frpmc.util;

import com.mefrp.frpmc.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Utility class for executing async API calls and sending results back to players.
 * Replaces raw Thread usage with Bukkit's scheduler for proper thread management.
 */
public class AsyncTask {

    /**
     * Execute an async API call and send result to player.
     */
    public static void executeAsync(Main plugin, CommandSender sender, AsyncApiCall apiCall, String loadingMsg) {
        if (loadingMsg != null && !loadingMsg.isEmpty()) {
            sender.sendMessage(loadingMsg);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                AsyncResult result = apiCall.call();

                // Send result back to player on main thread
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        result.sendTo(sender);
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Functional interface for async API calls.
     */
    @FunctionalInterface
    public interface AsyncApiCall {
        AsyncResult call();
    }

    /**
     * Result object containing success status, message, and optional data.
     */
    public static class AsyncResult {
        private final boolean success;
        private final String message;
        private final String data;
        private final ChatColor prefixColor;

        private AsyncResult(boolean success, String message, String data, ChatColor prefixColor) {
            this.success = success;
            this.message = message;
            this.data = data;
            this.prefixColor = prefixColor != null ? prefixColor : (success ? ChatColor.GREEN : ChatColor.RED);
        }

        public static AsyncResult success(String message) {
            return new AsyncResult(true, message, null, ChatColor.GREEN);
        }

        public static AsyncResult success(String message, String data) {
            return new AsyncResult(true, message, data, ChatColor.GREEN);
        }

        public static AsyncResult success(String message, String data, ChatColor prefixColor) {
            return new AsyncResult(true, message, data, prefixColor);
        }

        public static AsyncResult error(String message) {
            return new AsyncResult(false, message, null, ChatColor.RED);
        }

        public void sendTo(CommandSender sender) {
            sender.sendMessage(prefixColor + message);
            if (data != null && !data.isEmpty()) {
                sender.sendMessage(ChatColor.WHITE + data);
            }
        }
    }
}