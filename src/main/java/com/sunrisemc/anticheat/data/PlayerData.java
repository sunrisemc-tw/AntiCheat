package com.sunrisemc.anticheat.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 玩家數據類
 * 存儲單個玩家的挖礦歷史和統計信息
 */
public class PlayerData {
    
    private final List<MiningData> miningHistory;
    private int violationCount;
    private long lastViolationTime;
    
    // 飛行檢測相關
    private int flightViolations;
    private long hoverTime;
    
    // 速度檢測相關
    private int speedViolations;
    
    // 穿牆檢測相關
    private int noClipViolations;
    
    // 自動點擊檢測相關
    private final List<Long> clickHistory;
    private int autoClickViolations;
    
    // 無敵檢測相關
    private final List<DamageEvent> damageHistory;
    
    public PlayerData() {
        this.miningHistory = new CopyOnWriteArrayList<>();
        this.violationCount = 0;
        this.lastViolationTime = 0;
        
        // 初始化新的檢測相關變數
        this.flightViolations = 0;
        this.hoverTime = 0;
        this.speedViolations = 0;
        this.noClipViolations = 0;
        this.clickHistory = new CopyOnWriteArrayList<>();
        this.autoClickViolations = 0;
        this.damageHistory = new CopyOnWriteArrayList<>();
    }
    
    /**
     * 添加挖礦事件
     */
    public void addMiningEvent(MiningData miningData) {
        miningHistory.add(miningData);
    }
    
    /**
     * 獲取挖礦歷史
     */
    public List<MiningData> getMiningHistory() {
        return new ArrayList<>(miningHistory);
    }
    
    /**
     * 獲取最近的挖礦事件
     */
    public List<MiningData> getRecentMiningEvents(long timeWindow) {
        long currentTime = System.currentTimeMillis();
        List<MiningData> recent = new ArrayList<>();
        
        for (MiningData data : miningHistory) {
            if (currentTime - data.getTimestamp() <= timeWindow) {
                recent.add(data);
            }
        }
        
        return recent;
    }
    
    /**
     * 清理舊數據
     */
    public void cleanOldData(long maxAge) {
        long currentTime = System.currentTimeMillis();
        Iterator<MiningData> iterator = miningHistory.iterator();
        
        while (iterator.hasNext()) {
            MiningData data = iterator.next();
            if (currentTime - data.getTimestamp() > maxAge) {
                iterator.remove();
            }
        }
    }
    
    /**
     * 獲取挖礦事件數量
     */
    public int getMiningEventCount() {
        return miningHistory.size();
    }
    
    /**
     * 獲取違規次數
     */
    public int getViolationCount() {
        return violationCount;
    }
    
    /**
     * 增加違規次數
     */
    public void incrementViolationCount() {
        this.violationCount++;
        this.lastViolationTime = System.currentTimeMillis();
    }
    
    /**
     * 重置違規次數
     */
    public void resetViolationCount() {
        this.violationCount = 0;
    }
    
    /**
     * 獲取最後違規時間
     */
    public long getLastViolationTime() {
        return lastViolationTime;
    }
    
    /**
     * 計算平均挖礦間隔
     */
    public double getAverageMiningInterval() {
        if (miningHistory.size() < 2) {
            return 0;
        }
        
        long totalTime = 0;
        int intervals = 0;
        
        for (int i = 1; i < miningHistory.size(); i++) {
            long interval = miningHistory.get(i).getTimestamp() - miningHistory.get(i - 1).getTimestamp();
            if (interval > 0) {
                totalTime += interval;
                intervals++;
            }
        }
        
        return intervals > 0 ? (double) totalTime / intervals : 0;
    }
    
    /**
     * 獲取挖礦密度 (每秒挖礦次數)
     */
    public double getMiningDensity(long timeWindow) {
        List<MiningData> recent = getRecentMiningEvents(timeWindow);
        return recent.size() / (timeWindow / 1000.0);
    }
    
    /**
     * 檢查是否有足夠的數據進行分析
     */
    public boolean hasEnoughData(int minEvents) {
        return miningHistory.size() >= minEvents;
    }
    
    /**
     * 清空所有數據
     */
    public void clear() {
        miningHistory.clear();
        violationCount = 0;
        lastViolationTime = 0;
        
        // 清空新的檢測相關數據
        flightViolations = 0;
        hoverTime = 0;
        speedViolations = 0;
        noClipViolations = 0;
        clickHistory.clear();
        autoClickViolations = 0;
        damageHistory.clear();
    }
    
    // 飛行檢測相關方法
    public void incrementFlightViolations() {
        this.flightViolations++;
    }
    
    public void resetFlightViolations() {
        this.flightViolations = 0;
    }
    
    public int getFlightViolations() {
        return flightViolations;
    }
    
    public void incrementHoverTime() {
        this.hoverTime++;
    }
    
    public void resetHoverTime() {
        this.hoverTime = 0;
    }
    
    public long getHoverTime() {
        return hoverTime;
    }
    
    // 速度檢測相關方法
    public void incrementSpeedViolations() {
        this.speedViolations++;
    }
    
    public void resetSpeedViolations() {
        this.speedViolations = 0;
    }
    
    public int getSpeedViolations() {
        return speedViolations;
    }
    
    // 穿牆檢測相關方法
    public void incrementNoClipViolations() {
        this.noClipViolations++;
    }
    
    public void resetNoClipViolations() {
        this.noClipViolations = 0;
    }
    
    public int getNoClipViolations() {
        return noClipViolations;
    }
    
    // 自動點擊檢測相關方法
    public void addClickEvent(long timestamp) {
        clickHistory.add(timestamp);
    }
    
    public int getRecentClicks(long timeWindow) {
        long currentTime = System.currentTimeMillis();
        int count = 0;
        
        for (Long timestamp : clickHistory) {
            if (currentTime - timestamp <= timeWindow) {
                count++;
            }
        }
        
        return count;
    }
    
    public void incrementAutoClickViolations() {
        this.autoClickViolations++;
    }
    
    public void resetAutoClickViolations() {
        this.autoClickViolations = 0;
    }
    
    public int getAutoClickViolations() {
        return autoClickViolations;
    }
    
    // 無敵檢測相關方法
    public void addDamageEvent(long timestamp, double damage) {
        damageHistory.add(new DamageEvent(timestamp, damage));
    }
    
    public double getRecentDamage(long timeWindow) {
        long currentTime = System.currentTimeMillis();
        double totalDamage = 0;
        
        for (DamageEvent event : damageHistory) {
            if (currentTime - event.getTimestamp() <= timeWindow) {
                totalDamage += event.getDamage();
            }
        }
        
        return totalDamage;
    }
    
    // 內部類：傷害事件
    private static class DamageEvent {
        private final long timestamp;
        private final double damage;
        
        public DamageEvent(long timestamp, double damage) {
            this.timestamp = timestamp;
            this.damage = damage;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public double getDamage() {
            return damage;
        }
    }
}


