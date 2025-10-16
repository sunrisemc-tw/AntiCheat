package com.sunrisemc.anticheat.detection;

import com.sunrisemc.anticheat.AntiCheat;
import com.sunrisemc.anticheat.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * 穿牆檢測器
 * 檢測穿牆外掛和異常移動
 */
public class NoClipDetector {
    
    private final AntiCheat plugin;
    private double maxClipDistance;
    private int minViolations;
    
    public NoClipDetector(AntiCheat plugin) {
        this.plugin = plugin;
        reload();
    }
    
    /**
     * 重新載入配置
     */
    public void reload() {
        this.maxClipDistance = plugin.getConfigManager().getMaxClipDistance();
        this.minViolations = plugin.getConfigManager().getMinNoClipViolations();
    }
    
    /**
     * 檢測穿牆行為
     */
    public boolean detectNoClip(Player player, Location from, Location to, PlayerData data) {
        // 計算移動距離
        double distance = from.distance(to);
        
        // 如果移動距離很小，不需要檢測
        if (distance < 0.1) {
            return false;
        }
        
        // 檢查是否穿過固體方塊
        if (hasPassedThroughSolidBlocks(from, to)) {
            data.incrementNoClipViolations();
            
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info(String.format("穿牆檢測: %s 穿過固體方塊 違規次數: %d", 
                    player.getName(), data.getNoClipViolations()));
            }
            
            return data.getNoClipViolations() >= minViolations;
        } else {
            // 重置違規計數
            data.resetNoClipViolations();
        }
        
        return false;
    }
    
    /**
     * 檢查是否穿過固體方塊
     */
    private boolean hasPassedThroughSolidBlocks(Location from, Location to) {
        // 使用 Bresenham 算法檢查路徑上的方塊
        int steps = (int) Math.ceil(from.distance(to));
        
        for (int i = 0; i <= steps; i++) {
            double ratio = (double) i / steps;
            
            double x = from.getX() + (to.getX() - from.getX()) * ratio;
            double y = from.getY() + (to.getY() - from.getY()) * ratio;
            double z = from.getZ() + (to.getZ() - from.getZ()) * ratio;
            
            Location checkLocation = new Location(from.getWorld(), x, y, z);
            Material blockType = checkLocation.getBlock().getType();
            
            // 檢查是否為固體方塊
            if (isSolidBlock(blockType)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 檢查是否為固體方塊
     */
    private boolean isSolidBlock(Material material) {
        // 排除一些特殊情況
        if (material == Material.AIR || 
            material == Material.WATER || 
            material == Material.LAVA ||
            material == Material.TALL_GRASS ||
            material == Material.GRASS ||
            material == Material.DEAD_BUSH ||
            material == Material.FERN ||
            material == Material.LARGE_FERN ||
            material == Material.VINE ||
            material == Material.LADDER ||
            material == Material.SCAFFOLDING) {
            return false;
        }
        
        return material.isSolid();
    }
}
