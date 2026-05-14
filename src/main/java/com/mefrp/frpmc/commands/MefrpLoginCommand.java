package com.mefrp.frpmc.commands;

import com.mefrp.frpmc.Main;
import com.mefrp.frpmc.api.MefrpApiClient;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import com.mefrp.frpmc.util.AsyncTask;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class MefrpLoginCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;
    // 使用 ConcurrentHashMap 保证线程安全
    // key: playerName, value: [username, password, timestamp]
    private final ConcurrentMap<String, CaptchaInfo> pendingCaptcha = new ConcurrentHashMap<>();
    
    // 验证码有效期（毫秒）- 5分钟
    private static final long CAPTCHA_TIMEOUT_MS = 5 * 60 * 1000;
    // 清理任务ID
    private int cleanupTaskId = -1;

    public MefrpLoginCommand(Main plugin) {
        this.plugin = plugin;
        // 启动定时清理过期验证码的任务
        startCleanupTask();
    }
    
    /**
     * 启动定时清理过期验证码的任务
     */
    private void startCleanupTask() {
        cleanupTaskId = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            pendingCaptcha.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }, 60 * 20L, 60 * 20L).getTaskId(); // 每分钟检查一次
    }
    
    /**
     * 取消清理任务
     */
    public void cleanup() {
        if (cleanupTaskId != -1) {
            plugin.getServer().getScheduler().cancelTask(cleanupTaskId);
        }
    }
    
    // 待处理验证码的信息类
    private static class CaptchaInfo {
        final String username;
        final String password;
        final long timestamp;
        
        CaptchaInfo(String username, String password) {
            this.username = username;
            this.password = password;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CAPTCHA_TIMEOUT_MS;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("frpmc.command")) {
            sender.sendMessage(ChatColor.RED + " 你没有权限使用此命令。");
            return true;
        }

        // 检查是否启用验证码
        boolean captchaEnabled = plugin.getConfig().getBoolean("captcha.enabled", true);
        
        if (args.length == 1 && args[0].equalsIgnoreCase("captcha")) {
            // 处理验证码输入
            return handleCaptchaInput(sender, args);
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + " 用法: /mefrp login <用户名> <密码>");
            sender.sendMessage(ChatColor.YELLOW + " 或输入: /mefrp login captcha <验证码> 完成验证");
            return true;
        }

        String username = args[0];
        String password = args[1];

        if (captchaEnabled) {
            // 先进行人机验证
            String clientCode = plugin.getConfig().getString("captcha.client-code", "FrpMC");
            String captchaUrl = "https://www.mefrp.com/3rdparty/captcha?client=" + clientCode;
            
            sender.sendMessage(ChatColor.YELLOW + " === 人机验证 required ===");
            sender.sendMessage(ChatColor.GREEN + "请打开以下链接完成验证:");
            sender.sendMessage(ChatColor.AQUA + captchaUrl);
            sender.sendMessage(ChatColor.YELLOW + "完成验证后，将验证码输入: /mefrp login captcha <验证码>");
            
            // 保存登录信息稍后使用（带时间戳）
            pendingCaptcha.put(sender.getName(), new CaptchaInfo(username, password));
            
            // 如果是玩家，发送提示
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.sendMessage(ChatColor.GREEN + "[提示] 完成验证后将返回的验证码输入即可登录");
            }
        } else {
            // 跳过验证码直接登录
            performLogin(sender, username, password, null);
        }

        return true;
    }

    /**
     * 处理验证码输入
     */
    private boolean handleCaptchaInput(CommandSender sender, String[] args) {
        // 检查是否有待处理的登录
        CaptchaInfo info = pendingCaptcha.remove(sender.getName()); // 使用remove避免清理遗漏
        
        if (info == null) {
            sender.sendMessage(ChatColor.RED + " 没有待验证的登录请求。请先输入: /mefrp login <用户名> <密码>");
            return true;
        }
        
        // 检查是否过期（双重检查）
        if (info.isExpired()) {
            sender.sendMessage(ChatColor.RED + " 验证码已过期（5分钟）。请重新输入: /mefrp login <用户名> <密码>");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + " 用法: /mefrp login captcha <验证码>");
            // 重新放回队列
            pendingCaptcha.put(sender.getName(), info);
            return true;
        }

        String captchaCode = args[1];
        String expectedClient = plugin.getConfig().getString("captcha.client-code", "FrpMC");
        
        try {
            // Base64 解码
            String decoded = new String(Base64.getDecoder().decode(captchaCode));
            
            // 解析 token||client 格式
            String[] parts = decoded.split("\\|\\|");
            if (parts.length != 2) {
                sender.sendMessage(ChatColor.RED + " 验证码格式无效。");
                return true;
            }
            
            String token = parts[0];
            String client = parts[1];
            
            // 验证客户端代号是否匹配
            if (!client.equals(expectedClient)) {
                sender.sendMessage(ChatColor.RED + " 验证码不匹配此客户端。");
                return true;
            }
            
            // 执行登录
            performLogin(sender, info.username, info.password, token);
            
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + " 验证码无效，请重新验证。");
        }
        
        return true;
    }

    /**
     * 执行登录
     */
    private void performLogin(CommandSender sender, String username, String password, String captchaToken) {
        sender.sendMessage(ChatColor.YELLOW + " 正在登录到 Mefrp API...");

        AsyncTask.executeAsync(plugin, sender,
            () -> {
                MefrpApiClient.ApiResponse response;
                
                if (captchaToken != null && !captchaToken.isEmpty()) {
                    // 带验证码令牌登录
                    response = plugin.getApiClient().login(username, password, captchaToken);
                } else {
                    // 无验证码登录
                    response = plugin.getApiClient().login(username, password);
                }
                
                if (response.isSuccess()) {
                    // 获取用户信息
                    MefrpApiClient.ApiResponse userInfo = plugin.getApiClient().getUserInfo();
                    String welcomeMsg = "成功登录到 Mefrp!";
                    if (userInfo.isSuccess()) {
                        String apiUsername = userInfo.getString("username");
                        welcomeMsg = "成功登录到 Mefrp! 欢迎, " + apiUsername + "!";
                    }
                    return AsyncTask.AsyncResult.success(welcomeMsg);
                } else {
                    return AsyncTask.AsyncResult.error("登录失败: " + response.getMessage());
                }
            },
            null);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("captcha");
        }
        
        return completions;
    }
}