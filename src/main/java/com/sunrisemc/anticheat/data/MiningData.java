package com.sunrisemc.anticheat.data;

import org.bukkit.Location;
import org.bukkit.Material;

/**
 * 挖礦數據類
 * 記錄單次挖礦事件的詳細信息
 */
public class MiningData {
    
    private final Location location;
    private final Material blockType;
    private final Material toolType;
    private final long timestamp;
    
    public MiningData(Location location, Material blockType, Material toolType, long timestamp) {
        this.location = location.clone();
        this.blockType = blockType;
        this.toolType = toolType;
        this.timestamp = timestamp;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public Material getBlockType() {
        return blockType;
    }
    
    public Material getToolType() {
        return toolType;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * 計算與另一個挖礦位置的距離
     */
    public double distanceTo(MiningData other) {
        if (!location.getWorld().equals(other.location.getWorld())) {
            return Double.MAX_VALUE;
        }
        return location.distance(other.location);
    }
    
    /**
     * 計算時間差 (毫秒)
     */
    public long timeDifference(MiningData other) {
        return Math.abs(timestamp - other.timestamp);
    }
    
    @Override
    public String toString() {
        return String.format("MiningData{location=%s, block=%s, tool=%s, time=%d}", 
            location, blockType, toolType, timestamp);
    }
}
