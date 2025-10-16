package com.sunrisemc.anticheat.commands;

import com.sunrisemc.anticheat.AntiCheat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * AntiCheat 命令處理器
 */
public class AntiCheatCommand implements CommandExecutor {
    
    private final AntiCheat plugin;
    
    public AntiCheatCommand(AntiCheat plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("anticheat.admin")) {
            sender.sendMessage(ChatColor.RED + "您沒有權限使用此命令！");
            return true;
        }
        
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;
            case "info":
                handleInfo(sender);
                break;
            case "stats":
                handleStats(sender);
                break;
            case "help":
            default:
                sendHelpMessage(sender);
                break;
        }
        
        return true;
    }
    
    /**
     * 處理重新載入命令
     */
    private void handleReload(CommandSender sender) {
        try {
            plugin.reload();
            sender.sendMessage(ChatColor.GREEN + "AntiCheat 配置已重新載入！");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "重新載入配置時發生錯誤: " + e.getMessage());
            plugin.getLogger().severe("重新載入配置時發生錯誤: " + e.getMessage());
        }
    }
    
    /**
     * 處理信息命令
     */
    private void handleInfo(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== AntiCheat 插件信息 ===");
        sender.sendMessage(ChatColor.YELLOW + "版本: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.YELLOW + "作者: " + ChatColor.WHITE + plugin.getDescription().getAuthors());
        sender.sendMessage(ChatColor.YELLOW + "狀態: " + ChatColor.WHITE + (plugin.getConfigManager().isEnabled() ? "啟用" : "禁用"));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GREEN + "=== 檢測功能狀態 ===");
        sender.sendMessage(ChatColor.YELLOW + "挖礦軌跡檢測: " + ChatColor.WHITE + (plugin.getConfigManager().isMiningTraceEnabled() ? "啟用" : "禁用"));
        sender.sendMessage(ChatColor.YELLOW + "挖礦速度檢測: " + ChatColor.WHITE + (plugin.getConfigManager().isMiningSpeedEnabled() ? "啟用" : "禁用"));
        sender.sendMessage(ChatColor.YELLOW + "飛行檢測: " + ChatColor.WHITE + (plugin.getConfigManager().isFlightDetectionEnabled() ? "啟用" : "禁用"));
        sender.sendMessage(ChatColor.YELLOW + "速度檢測: " + ChatColor.WHITE + (plugin.getConfigManager().isSpeedDetectionEnabled() ? "啟用" : "禁用"));
        sender.sendMessage(ChatColor.YELLOW + "穿牆檢測: " + ChatColor.WHITE + (plugin.getConfigManager().isNoClipDetectionEnabled() ? "啟用" : "禁用"));
        sender.sendMessage(ChatColor.YELLOW + "自動點擊檢測: " + ChatColor.WHITE + (plugin.getConfigManager().isAutoClickDetectionEnabled() ? "啟用" : "禁用"));
        sender.sendMessage(ChatColor.YELLOW + "無敵檢測: " + ChatColor.WHITE + (plugin.getConfigManager().isGodModeDetectionEnabled() ? "啟用" : "禁用"));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GREEN + "=== 系統設定 ===");
        sender.sendMessage(ChatColor.YELLOW + "通知系統: " + ChatColor.WHITE + (plugin.getConfigManager().isNotificationEnabled() ? "啟用" : "禁用"));
        sender.sendMessage(ChatColor.YELLOW + "檢測間隔: " + ChatColor.WHITE + plugin.getConfigManager().getCheckInterval() + "ms");
        sender.sendMessage(ChatColor.GOLD + "========================");
    }
    
    /**
     * 處理統計命令
     */
    private void handleStats(CommandSender sender) {
        Map<String, Object> stats = plugin.getDetectionManager().getStats();
        
        sender.sendMessage(ChatColor.GOLD + "=== AntiCheat 統計信息 ===");
        sender.sendMessage(ChatColor.YELLOW + "活躍玩家: " + ChatColor.WHITE + stats.get("active_players"));
        sender.sendMessage(ChatColor.YELLOW + "冷卻中玩家: " + ChatColor.WHITE + stats.get("cooldown_players"));
        sender.sendMessage(ChatColor.YELLOW + "通知冷卻: " + ChatColor.WHITE + plugin.getNotificationManager().getActiveCooldowns());
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GREEN + "=== 檢測功能狀態 ===");
        sender.sendMessage(ChatColor.YELLOW + "挖礦軌跡檢測: " + ChatColor.WHITE + ((Boolean) stats.get("mining_trace_enabled") ? "啟用" : "禁用"));
        sender.sendMessage(ChatColor.YELLOW + "挖礦速度檢測: " + ChatColor.WHITE + ((Boolean) stats.get("mining_speed_enabled") ? "啟用" : "禁用"));
        sender.sendMessage(ChatColor.YELLOW + "飛行檢測: " + ChatColor.WHITE + (plugin.getConfigManager().isFlightDetectionEnabled() ? "啟用" : "禁用"));
        sender.sendMessage(ChatColor.YELLOW + "速度檢測: " + ChatColor.WHITE + (plugin.getConfigManager().isSpeedDetectionEnabled() ? "啟用" : "禁用"));
        sender.sendMessage(ChatColor.YELLOW + "穿牆檢測: " + ChatColor.WHITE + (plugin.getConfigManager().isNoClipDetectionEnabled() ? "啟用" : "禁用"));
        sender.sendMessage(ChatColor.YELLOW + "自動點擊檢測: " + ChatColor.WHITE + (plugin.getConfigManager().isAutoClickDetectionEnabled() ? "啟用" : "禁用"));
        sender.sendMessage(ChatColor.YELLOW + "無敵檢測: " + ChatColor.WHITE + (plugin.getConfigManager().isGodModeDetectionEnabled() ? "啟用" : "禁用"));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GREEN + "=== 管理員信息 ===");
        
        // 顯示在線管理員數量
        int adminCount = 0;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.hasPermission("anticheat.admin")) {
                adminCount++;
            }
        }
        sender.sendMessage(ChatColor.YELLOW + "在線管理員: " + ChatColor.WHITE + adminCount);
        sender.sendMessage(ChatColor.GOLD + "========================");
    }
    
    /**
     * 發送幫助訊息
     */
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== AntiCheat 命令幫助 ===");
        sender.sendMessage(ChatColor.YELLOW + "/anticheat reload" + ChatColor.WHITE + " - 重新載入配置");
        sender.sendMessage(ChatColor.YELLOW + "/anticheat info" + ChatColor.WHITE + " - 顯示插件信息");
        sender.sendMessage(ChatColor.YELLOW + "/anticheat stats" + ChatColor.WHITE + " - 顯示統計信息");
        sender.sendMessage(ChatColor.YELLOW + "/anticheat help" + ChatColor.WHITE + " - 顯示此幫助訊息");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GREEN + "=== 檢測功能 ===");
        sender.sendMessage(ChatColor.WHITE + "• 挖礦軌跡檢測 - 檢測直線挖礦和規律性挖礦");
        sender.sendMessage(ChatColor.WHITE + "• 挖礦速度檢測 - 檢測異常挖礦速度");
        sender.sendMessage(ChatColor.WHITE + "• 飛行檢測 - 檢測飛行外掛和懸浮");
        sender.sendMessage(ChatColor.WHITE + "• 速度檢測 - 檢測移動速度外掛");
        sender.sendMessage(ChatColor.WHITE + "• 穿牆檢測 - 檢測穿牆外掛");
        sender.sendMessage(ChatColor.WHITE + "• 自動點擊檢測 - 檢測自動點擊外掛");
        sender.sendMessage(ChatColor.WHITE + "• 無敵檢測 - 檢測無敵外掛");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GREEN + "=== 豁免條件 ===");
        sender.sendMessage(ChatColor.WHITE + "• 創造模式玩家");
        sender.sendMessage(ChatColor.WHITE + "• 旁觀者模式玩家");
        sender.sendMessage(ChatColor.WHITE + "• 擁有 anticheat.bypass 權限的玩家");
        sender.sendMessage(ChatColor.WHITE + "• 白名單中的玩家和世界");
        sender.sendMessage(ChatColor.GOLD + "========================");
    }
}
