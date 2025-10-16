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
    
    public PlayerData() {
        this.miningHistory = new CopyOnWriteArrayList<>();
        this.violationCount = 0;
        this.lastViolationTime = 0;
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
    }
}
