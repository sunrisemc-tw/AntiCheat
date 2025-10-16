package com.sunrisemc.anticheat.detection;

import com.sunrisemc.anticheat.AntiCheat;
import com.sunrisemc.anticheat.data.PlayerData;
import org.bukkit.entity.Player;

/**
 * 自動點擊檢測器
 * 檢測自動點擊外掛和異常點擊頻率
 */
public class AutoClickDetector {
    
    private final AntiCheat plugin;
    private int maxClicksPerSecond;
    private int minViolations;
    private long detectionWindow;
    
    public AutoClickDetector(AntiCheat plugin) {
        this.plugin = plugin;
        reload();
    }
    
    /**
     * 重新載入配置
     */
    public void reload() {
        this.maxClicksPerSecond = plugin.getConfigManager().getMaxClicksPerSecond();
        this.minViolations = plugin.getConfigManager().getMinAutoClickViolations();
        this.detectionWindow = plugin.getConfigManager().getAutoClickDetectionWindow();
    }
    
    /**
     * 檢測自動點擊行為
     */
    public boolean detectAutoClick(Player player, PlayerData data) {
        long currentTime = System.currentTimeMillis();
        
        // 記錄點擊事件
        data.addClickEvent(currentTime);
        
        // 獲取最近的點擊事件
        int recentClicks = data.getRecentClicks(detectionWindow);
        
        // 計算每秒點擊數
        double clicksPerSecond = recentClicks / (detectionWindow / 1000.0);
        
        // 檢查是否超過限制
        if (clicksPerSecond > maxClicksPerSecond) {
            data.incrementAutoClickViolations();
            
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info(String.format("自動點擊檢測: %s 每秒點擊: %.2f 最大允許: %d 違規次數: %d", 
                    player.getName(), clicksPerSecond, maxClicksPerSecond, data.getAutoClickViolations()));
            }
            
            return data.getAutoClickViolations() >= minViolations;
        } else {
            // 重置違規計數
            data.resetAutoClickViolations();
        }
        
        return false;
    }
}
