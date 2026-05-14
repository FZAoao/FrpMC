package com.mefrp.frpmc;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import com.mefrp.frpmc.commands.MefrpCommand;
import com.mefrp.frpmc.commands.MefrpLoginCommand;
import com.mefrp.frpmc.commands.MefrpStatusCommand;
import com.mefrp.frpmc.commands.MefrpTunnelCommand;
import com.mefrp.frpmc.api.MefrpApiClient;
import com.mefrp.frpmc.listeners.PlayerListener;
import com.mefrp.frpmc.tasks.StatusUpdateTask;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class Main extends JavaPlugin {

    private volatile static Main instance;
    private MefrpApiClient apiClient;
    private FileConfiguration messagesConfig;
    private File messagesFile;
    private StatusUpdateTask statusTask;

    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize API client
        String baseUrl = getConfig().getString("api.base-url", "https://api.mefrp.com/api");
        String userAgent = getConfig().getString("api.user-agent", "FrpMC/1.0.0 MinecraftPlugin");
        int timeout = getConfig().getInt("api.timeout", 10000);
        
        apiClient = new MefrpApiClient(baseUrl, userAgent, timeout);
        
        // Initialize messages config
        initMessagesConfig();
        
        // Register commands
        registerCommands();
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        // Start status update task if enabled
        if (getConfig().getBoolean("auto-reconnect.enabled", true)) {
            int interval = getConfig().getInt("status.update-interval", 60);
            statusTask = new StatusUpdateTask(this);
            statusTask.runTaskTimerAsynchronously(this, interval * 20L, interval * 20L);
        }
        
        getLogger().info("FrpMC v" + getDescription().getVersion() + " has been enabled!");
        getLogger().info("Mefrp API: " + baseUrl);
    }

    @Override
    public void onDisable() {
        if (statusTask != null) {
            statusTask.cancel();
        }
        
        // Cleanup login command tasks
        getServer().getScheduler().cancelTasks(this);
        
        if (apiClient != null && apiClient.isConnected()) {
            apiClient.disconnect();
        }
        
        getLogger().info("FrpMC has been disabled!");
    }

    private void initMessagesConfig() {
        messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            try {
                getDataFolder().mkdirs();
                messagesFile.createNewFile();
                // Copy default messages
                InputStream defaultStream = getResource("messages.yml");
                if (defaultStream != null) {
                    Files.copy(defaultStream, messagesFile.toPath());
                }
            } catch (IOException e) {
                getLogger().severe("Could not create messages.yml: " + e.getMessage());
            }
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private void registerCommands() {
        getCommand("mefrp").setExecutor(new MefrpCommand(this));
        getCommand("mefrp-login").setExecutor(new MefrpLoginCommand(this));
        getCommand("mefrp-tunnel").setExecutor(new MefrpTunnelCommand(this));
        getCommand("mefrp-status").setExecutor(new MefrpStatusCommand(this));
        
        // Set tab completers
        getCommand("mefrp").setTabCompleter(new MefrpCommand(this));
        getCommand("mefrp-login").setTabCompleter(new MefrpLoginCommand(this));
        getCommand("mefrp-tunnel").setTabCompleter(new MefrpTunnelCommand(this));
        getCommand("mefrp-status").setTabCompleter(new MefrpStatusCommand(this));
    }

    public static Main getInstance() {
        return instance;
    }
    
    /**
     * Safely get plugin instance for async contexts.
     */
    public static Main getInstanceSafe() {
        return instance;
    }

    public MefrpApiClient getApiClient() {
        return apiClient;
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }
    
    public String getMessage(String key) {
        return messagesConfig.getString(key, key);
    }
    
    /**
     * Get color-coded prefix for messages.
     * Note: messages.yml stores colors in & format, so we translate to § format.
     */
    public String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', 
            messagesConfig.getString("prefix", "&8[&6FrpMC&8]&r "));
    }
    
    /**
     * Reload plugin configuration and messages.
     * @return true if reload was successful
     */
    public boolean reloadPlugin() {
        try {
            // Reload main config
            reloadConfig();
            
            // Reinitialize API client with new config
            String baseUrl = getConfig().getString("api.base-url", "https://api.mefrp.com/api");
            String userAgent = getConfig().getString("api.user-agent", "FrpMC/1.0.0 MinecraftPlugin");
            int timeout = getConfig().getInt("api.timeout", 10000);
            
            // Create new API client and set token BEFORE assigning (thread-safe)
            String existingToken = apiClient.getToken();
            MefrpApiClient newClient = new MefrpApiClient(baseUrl, userAgent, timeout);
            if (existingToken != null && !existingToken.isEmpty()) {
                newClient.setToken(existingToken);
            }
            apiClient = newClient;
            
            // Reload messages config
            reloadMessagesConfig();
            
            // Restart status task if it was running
            if (getConfig().getBoolean("auto-reconnect.enabled", true)) {
                if (statusTask != null) {
                    statusTask.cancel();
                }
                int interval = getConfig().getInt("status.update-interval", 60);
                statusTask = new StatusUpdateTask(this);
                statusTask.runTaskTimerAsynchronously(this, interval * 20L, interval * 20L);
            }
            
            return true;
        } catch (Exception e) {
            getLogger().severe("Failed to reload plugin: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Reload messages configuration file.
     */
    public void reloadMessagesConfig() {
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }
}