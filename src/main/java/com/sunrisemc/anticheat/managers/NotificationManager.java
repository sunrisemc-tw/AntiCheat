package com.sunrisemc.anticheat.managers;

import com.sunrisemc.anticheat.AntiCheat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通知管理器
 * 負責向管理員發送檢測通知
 */
public class NotificationManager {
    
    private final AntiCheat plugin;
    private final Set<String> notificationCooldowns;
    
    public NotificationManager(AntiCheat plugin) {
        this.plugin = plugin;
        this.notificationCooldowns = ConcurrentHashMap.newKeySet();
    }
    
    /**
     * 通知管理員
     */
    public void notifyAdmins(Player player, String detectionType, String description) {
        if (!plugin.getConfigManager().isNotificationEnabled()) {
            return;
        }
        
        String playerName = player.getName();
        String cooldownKey = playerName + "_" + detectionType;
        
        // 檢查冷卻時間
        if (notificationCooldowns.contains(cooldownKey)) {
            return;
        }
        
        // 添加冷卻時間
        notificationCooldowns.add(cooldownKey);
        long cooldownMs = plugin.getConfigManager().getNotificationCooldown();
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            notificationCooldowns.remove(cooldownKey);
        }, cooldownMs / 50L);
        
        // 構建通知訊息
        String message = buildNotificationMessage(player, detectionType, description);
        
        // 發送控制台通知
        if (plugin.getConfigManager().isConsoleNotificationEnabled()) {
            sendConsoleNotification(message);
        }
        
        // 發送遊戲內通知
        if (plugin.getConfigManager().isInGameNotificationEnabled()) {
            sendInGameNotification(player, message);
        }
        
        // 未來: 發送 Webhook 通知
        if (plugin.getConfigManager().isWebhookEnabled()) {
            sendWebhookNotification(player, detectionType, description);
        }
    }
    
    /**
     * 構建通知訊息
     */
    private String buildNotificationMessage(Player player, String detectionType, String description) {
        StringBuilder message = new StringBuilder();
        message.append(ChatColor.RED).append("§l[AntiCheat] ").append(ChatColor.RESET);
        message.append(ChatColor.YELLOW).append("檢測到玩家異常行為").append(ChatColor.RESET).append("\n");
        message.append(ChatColor.GRAY).append("玩家: ").append(ChatColor.WHITE).append(player.getName()).append("\n");
        message.append(ChatColor.GRAY).append("類型: ").append(ChatColor.RED).append(detectionType).append("\n");
        message.append(ChatColor.GRAY).append("描述: ").append(ChatColor.WHITE).append(description).append("\n");
        message.append(ChatColor.GRAY).append("位置: ").append(ChatColor.WHITE)
               .append(formatLocation(player.getLocation())).append("\n");
        message.append(ChatColor.GRAY).append("時間: ").append(ChatColor.WHITE)
               .append(java.time.LocalTime.now().toString().substring(0, 8)).append("\n");
        message.append(ChatColor.BLUE).append("傳送請點我: ").append(ChatColor.WHITE)
               .append("[/tp ").append(player.getLocation().getBlockX()).append(" ")
               .append(player.getLocation().getBlockY()).append(" ")
               .append(player.getLocation().getBlockZ()).append("]");


        return message.toString();
    }
    
    /**
     * 格式化位置信息
     */
    private String formatLocation(org.bukkit.Location location) {
        return String.format("%s (%d, %d, %d)", 
            location.getWorld().getName(),
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ());
    }
    
    /**
     * 發送控制台通知
     */
    private void sendConsoleNotification(String message) {
        plugin.getLogger().warning("=== AntiCheat 檢測通知 ===");
        for (String line : message.split("\n")) {
            plugin.getLogger().warning(line.replaceAll("§[0-9a-fk-or]", ""));
        }
        plugin.getLogger().warning("========================");
    }
    
    /**
     * 發送遊戲內通知
     */
    private void sendInGameNotification(Player targetPlayer, String message) {
        int radius = plugin.getConfigManager().getNotificationRadius();
        
        // 確保在主線程中發送消息
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                // 檢查權限
                if (!player.hasPermission("anticheat.admin")) {
                    continue;
                }
                
                // 檢查距離
                if (radius > 0 && !player.getWorld().equals(targetPlayer.getWorld())) {
                    continue;
                }
                
                if (radius > 0) {
                    double distance = player.getLocation().distance(targetPlayer.getLocation());
                    if (distance > radius * 16) { // 16 blocks per chunk
                        continue;
                    }
                }
                
                // 發送通知
                player.sendMessage(message);
            }
        });
    }
    
    /**
     * 發送 Webhook 通知 (預留功能)
     */
    private void sendWebhookNotification(Player player, String detectionType, String description) {
        // 這裡可以實現 Discord/Slack 等 Webhook 通知
        // 為了避免硬體負載，使用異步處理
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // TODO: 實現 Webhook 發送邏輯
                plugin.getLogger().info("Webhook 通知功能尚未實現");
            } catch (Exception e) {
                plugin.getLogger().warning("發送 Webhook 通知時發生錯誤: " + e.getMessage());
            }
        });
    }
    
    /**
     * 重新載入通知管理器
     */
    public void reload() {
        notificationCooldowns.clear();
    }
    
    /**
     * 獲取通知統計
     */
    public int getActiveCooldowns() {
        return notificationCooldowns.size();
    }
}
