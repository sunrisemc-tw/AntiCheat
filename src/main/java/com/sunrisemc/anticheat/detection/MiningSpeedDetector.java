package com.sunrisemc.anticheat.detection;

import com.sunrisemc.anticheat.AntiCheat;
import com.sunrisemc.anticheat.data.MiningData;
import com.sunrisemc.anticheat.data.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 挖礦速度檢測器
 * 檢測異常的挖礦速度
 */
public class MiningSpeedDetector {
    
    private final AntiCheat plugin;
    private double normalSpeedMultiplier;
    private long detectionWindow;
    private int minDetectionCount;
    private Map<Material, Long> toolSpeeds;
    
    public MiningSpeedDetector(AntiCheat plugin) {
        this.plugin = plugin;
        reload();
    }
    
    /**
     * 重新載入配置
     */
    public void reload() {
        this.normalSpeedMultiplier = plugin.getConfigManager().getNormalSpeedMultiplier();
        this.detectionWindow = plugin.getConfigManager().getDetectionWindow();
        this.minDetectionCount = plugin.getConfigManager().getMinDetectionCount();
        this.toolSpeeds = plugin.getConfigManager().getToolSpeeds();
    }
    
    /**
     * 檢測可疑的挖礦速度
     */
    public boolean detectSuspiciousSpeed(PlayerData playerData, Player player) {
        List<MiningData> recentEvents = playerData.getRecentMiningEvents(detectionWindow);
        
        if (recentEvents.size() < minDetectionCount * 2) {
            return false; // 需要更多數據才能準確判斷
        }
        
        // 按工具類型分組檢測
        Map<Material, List<MiningData>> eventsByTool = groupEventsByTool(recentEvents);
        
        for (Map.Entry<Material, List<MiningData>> entry : eventsByTool.entrySet()) {
            Material tool = entry.getKey();
            List<MiningData> events = entry.getValue();
            
            if (events.size() < 5) { // 提高最小事件數要求
                continue;
            }
            
            // 檢測該工具的挖礦速度
            if (detectToolSpeedAnomaly(events, tool, player)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 按工具類型分組事件
     */
    private Map<Material, List<MiningData>> groupEventsByTool(List<MiningData> events) {
        Map<Material, List<MiningData>> grouped = new HashMap<>();
        
        for (MiningData data : events) {
            Material tool = data.getToolType();
            grouped.computeIfAbsent(tool, k -> new java.util.ArrayList<>()).add(data);
        }
        
        return grouped;
    }
    
    /**
     * 檢測特定工具的挖礦速度異常
     */
    private boolean detectToolSpeedAnomaly(List<MiningData> events, Material tool, Player player) {
        // 獲取該工具的預期挖礦時間
        long expectedTime = getExpectedMiningTime(tool, events.get(0).getBlockType());
        
        if (expectedTime <= 0) {
            return false; // 無法確定預期時間
        }
        
        // 計算實際平均挖礦間隔
        double actualInterval = calculateAverageInterval(events);
        
        if (actualInterval <= 0) {
            return false;
        }
        
        // 計算速度倍數
        double speedMultiplier = expectedTime / actualInterval;
        
        // 檢查是否超過正常速度倍數 (提高閾值，減少誤報)
        if (speedMultiplier > normalSpeedMultiplier * 1.5) {
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info(String.format("檢測到異常挖礦速度: %s 使用 %s, 速度倍數: %.2f", 
                    player.getName(), tool, speedMultiplier));
            }
            return true;
        }
        
        // 檢測連續快速挖礦
        if (detectConsecutiveFastMining(events, expectedTime)) {
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info("檢測到連續快速挖礦: " + player.getName());
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * 獲取預期的挖礦時間
     */
    private long getExpectedMiningTime(Material tool, Material block) {
        // 首先檢查配置中的工具速度
        if (toolSpeeds.containsKey(tool)) {
            return toolSpeeds.get(tool);
        }
        
        // 使用默認的 Minecraft 挖礦時間
        return getDefaultMiningTime(tool, block);
    }
    
    /**
     * 獲取默認的挖礦時間 (毫秒)
     */
    private long getDefaultMiningTime(Material tool, Material block) {
        // 基礎挖礦時間 (以石頭為基準)
        long baseTime = 1500; // 1.5秒
        
        // 根據工具調整
        switch (tool) {
            case DIAMOND_PICKAXE:
            case NETHERITE_PICKAXE:
                baseTime = 1000;
                break;
            case IRON_PICKAXE:
                baseTime = 1200;
                break;
            case STONE_PICKAXE:
                baseTime = 2000;
                break;
            case WOODEN_PICKAXE:
                baseTime = 3000;
                break;
            case GOLDEN_PICKAXE:
                baseTime = 600;
                break;
            default:
                baseTime = 2000;
                break;
        }
        
        // 根據方塊類型調整
        switch (block) {
            case STONE:
            case COBBLESTONE:
                return baseTime;
            case IRON_ORE:
            case DEEPSLATE_IRON_ORE:
                return baseTime * 3;
            case GOLD_ORE:
            case DEEPSLATE_GOLD_ORE:
                return baseTime * 3;
            case DIAMOND_ORE:
            case DEEPSLATE_DIAMOND_ORE:
                return baseTime * 15;
            case EMERALD_ORE:
            case DEEPSLATE_EMERALD_ORE:
                return baseTime * 20;
            case COAL_ORE:
            case DEEPSLATE_COAL_ORE:
                return baseTime;
            case REDSTONE_ORE:
            case DEEPSLATE_REDSTONE_ORE:
                return baseTime * 3;
            case LAPIS_ORE:
            case DEEPSLATE_LAPIS_ORE:
                return baseTime * 3;
            case NETHER_GOLD_ORE:
                return baseTime * 2;
            case NETHER_QUARTZ_ORE:
                return baseTime * 2;
            case ANCIENT_DEBRIS:
                return baseTime * 30;
            default:
                return baseTime;
        }
    }
    
    /**
     * 計算平均挖礦間隔
     */
    private double calculateAverageInterval(List<MiningData> events) {
        if (events.size() < 2) {
            return 0;
        }
        
        long totalInterval = 0;
        int intervalCount = 0;
        
        for (int i = 1; i < events.size(); i++) {
            long interval = events.get(i).getTimestamp() - events.get(i - 1).getTimestamp();
            if (interval > 0 && interval < 10000) { // 過濾異常大的間隔
                totalInterval += interval;
                intervalCount++;
            }
        }
        
        return intervalCount > 0 ? (double) totalInterval / intervalCount : 0;
    }
    
    /**
     * 檢測連續快速挖礦
     */
    private boolean detectConsecutiveFastMining(List<MiningData> events, long expectedTime) {
        if (events.size() < 3) {
            return false;
        }
        
        int consecutiveFastCount = 0;
        int maxConsecutive = 0;
        
        for (int i = 1; i < events.size(); i++) {
            long interval = events.get(i).getTimestamp() - events.get(i - 1).getTimestamp();
            
            // 如果間隔小於預期時間的一半，視為快速挖礦
            if (interval < expectedTime / 2) {
                consecutiveFastCount++;
                maxConsecutive = Math.max(maxConsecutive, consecutiveFastCount);
            } else {
                consecutiveFastCount = 0;
            }
        }
        
        // 如果連續快速挖礦次數過多，視為異常 (提高閾值)
        return maxConsecutive >= 5;
    }
    
    /**
     * 檢測特定方塊的挖礦速度
     */
    public boolean detectSpecificBlockSpeed(PlayerData playerData, Material blockType, Player player) {
        List<MiningData> recentEvents = playerData.getRecentMiningEvents(detectionWindow);
        
        // 過濾特定方塊類型
        List<MiningData> filteredEvents = new java.util.ArrayList<>();
        for (MiningData data : recentEvents) {
            if (data.getBlockType() == blockType) {
                filteredEvents.add(data);
            }
        }
        
        if (filteredEvents.size() < minDetectionCount) {
            return false;
        }
        
        // 按工具分組檢測
        Map<Material, List<MiningData>> eventsByTool = groupEventsByTool(filteredEvents);
        
        for (Map.Entry<Material, List<MiningData>> entry : eventsByTool.entrySet()) {
            Material tool = entry.getKey();
            List<MiningData> events = entry.getValue();
            
            if (events.size() < 2) {
                continue;
            }
            
            if (detectToolSpeedAnomaly(events, tool, player)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 獲取玩家的挖礦統計
     */
    public Map<String, Object> getMiningStats(PlayerData playerData) {
        Map<String, Object> stats = new HashMap<>();
        
        List<MiningData> recentEvents = playerData.getRecentMiningEvents(detectionWindow);
        
        if (recentEvents.isEmpty()) {
            return stats;
        }
        
        // 計算平均間隔
        double avgInterval = calculateAverageInterval(recentEvents);
        stats.put("average_interval", avgInterval);
        
        // 計算挖礦密度
        double density = recentEvents.size() / (detectionWindow / 1000.0);
        stats.put("mining_density", density);
        
        // 按工具統計
        Map<Material, Integer> toolCounts = new HashMap<>();
        for (MiningData data : recentEvents) {
            toolCounts.merge(data.getToolType(), 1, Integer::sum);
        }
        stats.put("tool_usage", toolCounts);
        
        return stats;
    }
}

