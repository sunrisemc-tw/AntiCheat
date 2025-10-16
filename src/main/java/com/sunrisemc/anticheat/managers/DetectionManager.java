package com.sunrisemc.anticheat.managers;

import com.sunrisemc.anticheat.AntiCheat;
import com.sunrisemc.anticheat.data.MiningData;
import com.sunrisemc.anticheat.data.PlayerData;
import com.sunrisemc.anticheat.detection.MiningTraceDetector;
import com.sunrisemc.anticheat.detection.MiningSpeedDetector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 檢測管理器
 * 負責管理所有檢測功能和玩家數據
 */
public class DetectionManager {
    
    private final AntiCheat plugin;
    private final Map<UUID, PlayerData> playerDataMap;
    private final MiningTraceDetector traceDetector;
    private final MiningSpeedDetector speedDetector;
    private final Set<UUID> cooldownPlayers;
    
    public DetectionManager(AntiCheat plugin) {
        this.plugin = plugin;
        this.playerDataMap = new ConcurrentHashMap<>();
        this.traceDetector = new MiningTraceDetector(plugin);
        this.speedDetector = new MiningSpeedDetector(plugin);
        this.cooldownPlayers = ConcurrentHashMap.newKeySet();
    }
    
    /**
     * 獲取玩家數據
     */
    public PlayerData getPlayerData(UUID playerId) {
        return playerDataMap.computeIfAbsent(playerId, k -> new PlayerData());
    }
    
    /**
     * 記錄挖礦事件
     */
    public void recordMiningEvent(Player player, Location location, Material blockType, Material toolType) {
        if (!plugin.getConfigManager().isEnabled()) {
            return;
        }
        
        // 檢查白名單
        if (isPlayerWhitelisted(player)) {
            return;
        }
        
        PlayerData data = getPlayerData(player.getUniqueId());
        MiningData miningData = new MiningData(location, blockType, toolType, System.currentTimeMillis());
        
        data.addMiningEvent(miningData);
        
        // 清理舊數據以節省記憶體
        data.cleanOldData(plugin.getConfigManager().getDetectionWindow());
    }
    
    /**
     * 檢查挖礦軌跡
     */
    public void checkMiningTraces() {
        if (!plugin.getConfigManager().isMiningTraceEnabled()) {
            return;
        }
        
        for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            UUID playerId = entry.getKey();
            PlayerData data = entry.getValue();
            
            if (isPlayerInCooldown(playerId)) {
                continue;
            }
            
            Player player = Bukkit.getPlayer(playerId);
            if (player == null || !player.isOnline()) {
                continue;
            }
            
            if (traceDetector.detectSuspiciousTrace(data, player)) {
                handleDetection(player, "挖礦軌跡疑似異常", "檢測到不自然的挖礦軌跡");
                addCooldown(playerId, plugin.getConfigManager().getTraceCooldown());
            }
        }
    }
    
    /**
     * 檢查挖礦速度
     */
    public void checkMiningSpeeds() {
        if (!plugin.getConfigManager().isMiningSpeedEnabled()) {
            return;
        }
        
        for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            UUID playerId = entry.getKey();
            PlayerData data = entry.getValue();
            
            if (isPlayerInCooldown(playerId)) {
                continue;
            }
            
            Player player = Bukkit.getPlayer(playerId);
            if (player == null || !player.isOnline()) {
                continue;
            }
            
            if (speedDetector.detectSuspiciousSpeed(data, player)) {
                handleDetection(player, "挖礦速度異常", "檢測到異常的挖礦速度");
                addCooldown(playerId, plugin.getConfigManager().getNotificationCooldown());
            }
        }
    }
    
    /**
     * 處理檢測結果
     */
    private void handleDetection(Player player, String detectionType, String description) {
        plugin.getNotificationManager().notifyAdmins(player, detectionType, description);
        
        // 記錄到日誌
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info(String.format("檢測到 %s: %s (%s) - %s", 
                detectionType, player.getName(), player.getUniqueId(), description));
        }
        
        // 執行懲罰 (如果啟用)
        if (plugin.getConfigManager().isPunishmentEnabled()) {
            executePunishment(player, detectionType);
        }
    }
    
    /**
     * 執行懲罰
     */
    private void executePunishment(Player player, String detectionType) {
        PlayerData data = getPlayerData(player.getUniqueId());
        data.incrementViolationCount();
        
        int warningCount = plugin.getConfigManager().getWarningCount();
        if (data.getViolationCount() >= warningCount) {
            String punishmentType = plugin.getConfigManager().getPunishmentType();
            String message = plugin.getConfigManager().getPunishmentMessage();
            
            // 確保懲罰在主線程中執行
            Bukkit.getScheduler().runTask(plugin, () -> {
                switch (punishmentType.toUpperCase()) {
                    case "KICK":
                        player.kickPlayer(org.bukkit.ChatColor.translateAlternateColorCodes('&', message));
                        break;
                    case "BAN":
                        Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(player.getName(), message, null, null);
                        player.kickPlayer(org.bukkit.ChatColor.translateAlternateColorCodes('&', message));
                        break;
                    case "WARN":
                    default:
                        player.sendMessage("§c[AntiCheat] " + message);
                        break;
                }
            });
            
            // 重置違規計數
            data.resetViolationCount();
        }
    }
    
    /**
     * 檢查玩家是否在白名單中
     */
    private boolean isPlayerWhitelisted(Player player) {
        if (!plugin.getConfigManager().isWhitelistEnabled()) {
            return false;
        }
        
        // 檢查玩家白名單
        if (plugin.getConfigManager().getWhitelistedPlayers().contains(player.getName()) ||
            plugin.getConfigManager().getWhitelistedPlayers().contains(player.getUniqueId().toString())) {
            return true;
        }
        
        // 檢查世界白名單
        if (plugin.getConfigManager().getWhitelistedWorlds().contains(player.getWorld().getName())) {
            return true;
        }
        
        // 檢查權限
        return player.hasPermission("anticheat.bypass");
    }
    
    /**
     * 檢查玩家是否在冷卻中
     */
    private boolean isPlayerInCooldown(UUID playerId) {
        return cooldownPlayers.contains(playerId);
    }
    
    /**
     * 添加冷卻時間
     */
    private void addCooldown(UUID playerId, long cooldownMs) {
        cooldownPlayers.add(playerId);
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            cooldownPlayers.remove(playerId);
        }, cooldownMs / 50L);
    }
    
    /**
     * 重新載入檢測器
     */
    public void reload() {
        // 清理所有玩家數據
        playerDataMap.clear();
        cooldownPlayers.clear();
        
        // 重新初始化檢測器
        traceDetector.reload();
        speedDetector.reload();
    }
    
    /**
     * 關閉檢測管理器
     */
    public void shutdown() {
        playerDataMap.clear();
        cooldownPlayers.clear();
    }
    
    /**
     * 獲取統計信息
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("active_players", playerDataMap.size());
        stats.put("cooldown_players", cooldownPlayers.size());
        stats.put("mining_trace_enabled", plugin.getConfigManager().isMiningTraceEnabled());
        stats.put("mining_speed_enabled", plugin.getConfigManager().isMiningSpeedEnabled());
        return stats;
    }
}
