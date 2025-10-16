package com.sunrisemc.anticheat.detection;

import com.sunrisemc.anticheat.AntiCheat;
import com.sunrisemc.anticheat.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

/**
 * 速度檢測器
 * 檢測速度外掛和異常移動速度
 */
public class SpeedDetector {
    
    private final AntiCheat plugin;
    private double maxSpeed;
    private double maxSprintSpeed;
    private int minViolations;
    
    public SpeedDetector(AntiCheat plugin) {
        this.plugin = plugin;
        reload();
    }
    
    /**
     * 重新載入配置
     */
    public void reload() {
        this.maxSpeed = plugin.getConfigManager().getMaxSpeed();
        this.maxSprintSpeed = plugin.getConfigManager().getMaxSprintSpeed();
        this.minViolations = plugin.getConfigManager().getMinSpeedViolations();
    }
    
    /**
     * 檢測速度行為
     */
    public boolean detectSpeed(Player player, Location from, Location to, PlayerData data) {
        // 檢查是否在液體中
        if (from.getBlock().isLiquid() || to.getBlock().isLiquid()) {
            return false;
        }
        
        // 檢查是否在冰上
        if (isOnIce(from) || isOnIce(to)) {
            return false;
        }
        
        // 計算移動距離
        double deltaX = to.getX() - from.getX();
        double deltaZ = to.getZ() - from.getZ();
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        
        // 計算速度 (blocks per tick)
        double speed = distance;
        
        // 獲取速度藥水效果
        double speedMultiplier = 1.0;
        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
            int level = player.getPotionEffect(PotionEffectType.SPEED).getAmplifier() + 1;
            speedMultiplier = 1.0 + (level * 0.2);
        }
        
        // 檢查是否在衝刺
        boolean isSprinting = player.isSprinting();
        double maxAllowedSpeed = isSprinting ? maxSprintSpeed : maxSpeed;
        
        // 應用速度藥水倍數
        maxAllowedSpeed *= speedMultiplier;
        
        // 檢查速度是否超標
        if (speed > maxAllowedSpeed) {
            data.incrementSpeedViolations();
            
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info(String.format("速度檢測: %s 速度: %.3f 最大允許: %.3f 違規次數: %d", 
                    player.getName(), speed, maxAllowedSpeed, data.getSpeedViolations()));
            }
            
            return data.getSpeedViolations() >= minViolations;
        } else {
            // 重置違規計數
            data.resetSpeedViolations();
        }
        
        return false;
    }
    
    /**
     * 檢查是否在冰上
     */
    private boolean isOnIce(Location location) {
        String blockType = location.getBlock().getType().name();
        return blockType.contains("ICE") || blockType.contains("FROSTED_ICE");
    }
}
