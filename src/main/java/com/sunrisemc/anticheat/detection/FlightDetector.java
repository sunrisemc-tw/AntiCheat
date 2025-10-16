package com.sunrisemc.anticheat.detection;

import com.sunrisemc.anticheat.AntiCheat;
import com.sunrisemc.anticheat.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

/**
 * 飛行檢測器
 * 檢測飛行外掛和異常飛行行為
 */
public class FlightDetector {
    
    private final AntiCheat plugin;
    private double maxVerticalSpeed;
    private double maxHorizontalSpeed;
    private int minFlightTime;
    
    public FlightDetector(AntiCheat plugin) {
        this.plugin = plugin;
        reload();
    }
    
    /**
     * 重新載入配置
     */
    public void reload() {
        this.maxVerticalSpeed = plugin.getConfigManager().getMaxVerticalSpeed();
        this.maxHorizontalSpeed = plugin.getConfigManager().getMaxHorizontalSpeed();
        this.minFlightTime = plugin.getConfigManager().getMinFlightTime();
    }
    
    /**
     * 檢測飛行行為
     */
    public boolean detectFlight(Player player, Location from, Location to, PlayerData data) {
        // 檢查玩家是否允許飛行
        if (player.getAllowFlight() || player.isFlying()) {
            return false;
        }
        
        // 檢查是否有飛行藥水效果
        if (player.hasPotionEffect(PotionEffectType.LEVITATION)) {
            return false;
        }
        
        // 檢查是否在液體中
        if (from.getBlock().isLiquid() || to.getBlock().isLiquid()) {
            return false;
        }
        
        // 檢查是否在梯子或藤蔓上
        if (isOnClimbable(from) || isOnClimbable(to)) {
            return false;
        }
        
        // 計算移動距離
        double deltaX = to.getX() - from.getX();
        double deltaY = to.getY() - from.getY();
        double deltaZ = to.getZ() - from.getZ();
        
        // 計算速度
        double horizontalSpeed = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double verticalSpeed = Math.abs(deltaY);
        
        // 檢查垂直速度
        if (verticalSpeed > maxVerticalSpeed && deltaY > 0) {
            data.incrementFlightViolations();
            return data.getFlightViolations() >= 3;
        }
        
        // 檢查水平速度（在空中時）
        if (!isOnGround(to) && horizontalSpeed > maxHorizontalSpeed) {
            data.incrementFlightViolations();
            return data.getFlightViolations() >= 3;
        }
        
        // 檢查懸浮
        if (detectHovering(player, from, to, data)) {
            return true;
        }
        
        // 重置違規計數（如果沒有違規）
        if (verticalSpeed <= maxVerticalSpeed && horizontalSpeed <= maxHorizontalSpeed) {
            data.resetFlightViolations();
        }
        
        return false;
    }
    
    /**
     * 檢測飛行切換
     */
    public boolean detectToggleFlight(Player player, boolean isFlying, PlayerData data) {
        // 如果玩家沒有飛行權限但能切換飛行，可能為外掛
        if (isFlying && !player.getAllowFlight()) {
            data.incrementFlightViolations();
            return data.getFlightViolations() >= 2;
        }
        
        return false;
    }
    
    /**
     * 檢測懸浮行為
     */
    private boolean detectHovering(Player player, Location from, Location to, PlayerData data) {
        // 檢查是否在空中懸浮
        if (!isOnGround(from) && !isOnGround(to)) {
            double deltaY = Math.abs(to.getY() - from.getY());
            
            // 如果垂直移動很小，可能為懸浮
            if (deltaY < 0.1) {
                data.incrementHoverTime();
                
                // 如果懸浮時間超過閾值
                if (data.getHoverTime() > minFlightTime) {
                    return true;
                }
            } else {
                data.resetHoverTime();
            }
        } else {
            data.resetHoverTime();
        }
        
        return false;
    }
    
    /**
     * 檢查是否在地面上
     */
    private boolean isOnGround(Location location) {
        Location groundCheck = location.clone().subtract(0, 0.1, 0);
        return groundCheck.getBlock().getType().isSolid();
    }
    
    /**
     * 檢查是否在可攀爬物體上
     */
    private boolean isOnClimbable(Location location) {
        return location.getBlock().getType().name().contains("LADDER") ||
               location.getBlock().getType().name().contains("VINE") ||
               location.getBlock().getType().name().contains("SCAFFOLDING");
    }
}
