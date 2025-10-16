package com.sunrisemc.anticheat.detection;

import com.sunrisemc.anticheat.AntiCheat;
import com.sunrisemc.anticheat.data.MiningData;
import com.sunrisemc.anticheat.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 挖礦軌跡檢測器
 * 檢測不自然的挖礦軌跡模式
 */
public class MiningTraceDetector {
    
    private final AntiCheat plugin;
    private int sensitivity;
    private int maxTraceLength;
    
    public MiningTraceDetector(AntiCheat plugin) {
        this.plugin = plugin;
        reload();
    }
    
    /**
     * 重新載入配置
     */
    public void reload() {
        this.sensitivity = plugin.getConfigManager().getTraceSensitivity();
        this.maxTraceLength = plugin.getConfigManager().getMaxTraceLength();
    }
    
    /**
     * 檢測可疑的挖礦軌跡
     */
    public boolean detectSuspiciousTrace(PlayerData playerData, Player player) {
        List<MiningData> recentEvents = playerData.getRecentMiningEvents(60000); // 60秒內的數據
        
        if (recentEvents.size() < 10) {
            return false; // 需要更多數據才能準確判斷
        }
        
        // 檢查是否在短時間內有太多挖礦事件
        if (recentEvents.size() > 50) {
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info("檢測到過多挖礦事件: " + player.getName() + " (" + recentEvents.size() + " 次)");
            }
            return true;
        }
        
        // 檢測直線挖礦軌跡 (提高閾值)
        if (detectLinearMining(recentEvents)) {
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info("檢測到直線挖礦軌跡: " + player.getName());
            }
            return true;
        }
        
        // 檢測規律性挖礦模式 (提高閾值)
        if (detectPatternMining(recentEvents)) {
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info("檢測到規律性挖礦模式: " + player.getName());
            }
            return true;
        }
        
        // 檢測異常的挖礦密度 (降低敏感度)
        if (detectAbnormalDensity(recentEvents)) {
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info("檢測到異常挖礦密度: " + player.getName());
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * 檢測直線挖礦軌跡
     */
    private boolean detectLinearMining(List<MiningData> events) {
        if (events.size() < 5) {
            return false;
        }
        
        // 檢查是否在直線上挖礦
        List<Location> locations = new ArrayList<>();
        for (MiningData data : events) {
            locations.add(data.getLocation());
        }
        
        // 檢查是否為垂直挖礦 (直直往下挖)
        if (isVerticalMining(locations)) {
            // 垂直挖礦需要更嚴格的檢測
            return detectVerticalMiningPattern(locations);
        }
        
        // 計算線性度
        double linearity = calculateLinearity(locations);
        
        // 根據敏感度調整閾值 (提高閾值，減少誤報)
        double threshold = 0.9 + (sensitivity - 5) * 0.03;
        
        return linearity > threshold;
    }
    
    /**
     * 檢查是否為垂直挖礦
     */
    private boolean isVerticalMining(List<Location> locations) {
        if (locations.size() < 3) {
            return false;
        }
        
        // 檢查 X 和 Z 座標是否基本不變
        double xVariance = calculateVariance(locations, l -> l.getX());
        double zVariance = calculateVariance(locations, l -> l.getZ());
        
        // 如果 X 和 Z 的變異很小，且 Y 座標在下降，則為垂直挖礦
        return xVariance < 2.0 && zVariance < 2.0 && isDescending(locations);
    }
    
    /**
     * 檢測垂直挖礦模式
     */
    private boolean detectVerticalMiningPattern(List<Location> locations) {
        if (locations.size() < 10) {
            return false; // 垂直挖礦需要更多數據
        }
        
        // 檢查是否連續下降超過一定距離
        double totalDescent = locations.get(0).getY() - locations.get(locations.size() - 1).getY();
        
        // 如果下降距離超過 20 格，且挖礦次數很多，則可能為外掛
        return totalDescent > 20 && locations.size() > 15;
    }
    
    /**
     * 檢查是否為下降趨勢
     */
    private boolean isDescending(List<Location> locations) {
        if (locations.size() < 2) {
            return false;
        }
        
        double firstY = locations.get(0).getY();
        double lastY = locations.get(locations.size() - 1).getY();
        
        return lastY < firstY;
    }
    
    /**
     * 計算變異數
     */
    private double calculateVariance(List<Location> locations, java.util.function.Function<Location, Double> extractor) {
        if (locations.size() < 2) {
            return 0;
        }
        
        double sum = 0;
        for (Location loc : locations) {
            sum += extractor.apply(loc);
        }
        double mean = sum / locations.size();
        
        double variance = 0;
        for (Location loc : locations) {
            variance += Math.pow(extractor.apply(loc) - mean, 2);
        }
        
        return variance / locations.size();
    }
    
    /**
     * 計算軌跡的線性度
     */
    private double calculateLinearity(List<Location> locations) {
        if (locations.size() < 3) {
            return 0;
        }
        
        // 使用最小二乘法計算線性度
        double sumX = 0, sumY = 0, sumZ = 0;
        double sumXX = 0, sumXY = 0, sumXZ = 0;
        double sumYY = 0, sumYZ = 0, sumZZ = 0;
        
        int n = locations.size();
        
        for (int i = 0; i < n; i++) {
            Location loc = locations.get(i);
            double x = loc.getX();
            double y = loc.getY();
            double z = loc.getZ();
            
            sumX += x;
            sumY += y;
            sumZ += z;
            sumXX += x * x;
            sumXY += x * y;
            sumXZ += x * z;
            sumYY += y * y;
            sumYZ += y * z;
            sumZZ += z * z;
        }
        
        // 計算相關係數
        double meanX = sumX / n;
        double meanY = sumY / n;
        double meanZ = sumZ / n;
        
        double numerator = 0;
        double denomX = 0, denomY = 0, denomZ = 0;
        
        for (Location loc : locations) {
            double x = loc.getX() - meanX;
            double y = loc.getY() - meanY;
            double z = loc.getZ() - meanZ;
            
            numerator += x * y + x * z + y * z;
            denomX += x * x;
            denomY += y * y;
            denomZ += z * z;
        }
        
        double denominator = Math.sqrt(denomX * denomY * denomZ);
        return denominator > 0 ? Math.abs(numerator / denominator) : 0;
    }
    
    /**
     * 檢測規律性挖礦模式
     */
    private boolean detectPatternMining(List<MiningData> events) {
        if (events.size() < 6) {
            return false;
        }
        
        // 檢查時間間隔的規律性
        List<Long> intervals = new ArrayList<>();
        for (int i = 1; i < events.size(); i++) {
            long interval = events.get(i).getTimestamp() - events.get(i - 1).getTimestamp();
            intervals.add(interval);
        }
        
        // 計算間隔的變異係數
        double coefficient = calculateCoefficientOfVariation(intervals);
        
        // 變異係數越小，表示越規律 (提高閾值，減少誤報)
        double threshold = 0.2 - (sensitivity - 5) * 0.03;
        
        return coefficient < threshold;
    }
    
    /**
     * 計算變異係數
     */
    private double calculateCoefficientOfVariation(List<Long> values) {
        if (values.isEmpty()) {
            return 0;
        }
        
        double sum = 0;
        for (Long value : values) {
            sum += value;
        }
        double mean = sum / values.size();
        
        double variance = 0;
        for (Long value : values) {
            variance += Math.pow(value - mean, 2);
        }
        variance /= values.size();
        
        double stdDev = Math.sqrt(variance);
        return mean > 0 ? stdDev / mean : 0;
    }
    
    /**
     * 檢測異常的挖礦密度
     */
    private boolean detectAbnormalDensity(List<MiningData> events) {
        if (events.size() < 3) {
            return false;
        }
        
        // 計算挖礦密度 (每秒挖礦次數)
        long timeSpan = events.get(events.size() - 1).getTimestamp() - events.get(0).getTimestamp();
        if (timeSpan <= 0) {
            return false;
        }
        
        double density = events.size() / (timeSpan / 1000.0);
        
        // 根據敏感度調整密度閾值 (提高閾值，減少誤報)
        double threshold = 3.0 + (sensitivity - 5) * 0.3;
        
        return density > threshold;
    }
    
    /**
     * 檢測特定方塊類型的挖礦模式
     */
    public boolean detectSpecificBlockMining(PlayerData playerData, Material targetBlock) {
        List<MiningData> recentEvents = playerData.getRecentMiningEvents(60000); // 1分鐘內的數據
        
        // 過濾特定方塊類型
        List<MiningData> filteredEvents = new ArrayList<>();
        for (MiningData data : recentEvents) {
            if (data.getBlockType() == targetBlock) {
                filteredEvents.add(data);
            }
        }
        
        if (filteredEvents.size() < 3) {
            return false;
        }
        
        // 檢查是否過於頻繁地挖掘同一種方塊
        double density = filteredEvents.size() / 60.0; // 每分鐘挖掘次數
        double threshold = 1.0 + (sensitivity - 5) * 0.2;
        
        return density > threshold;
    }
}

