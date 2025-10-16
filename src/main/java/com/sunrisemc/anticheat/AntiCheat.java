package com.sunrisemc.anticheat;

import com.sunrisemc.anticheat.commands.AntiCheatCommand;
import com.sunrisemc.anticheat.listeners.MiningListener;
import com.sunrisemc.anticheat.managers.ConfigManager;
import com.sunrisemc.anticheat.managers.DetectionManager;
import com.sunrisemc.anticheat.managers.NotificationManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * AntiCheat 主插件類
 * 主要功能：防止外掛，檢測挖礦軌跡和挖礦速度
 */
public class AntiCheat extends JavaPlugin {
    
    private static AntiCheat instance;
    private ConfigManager configManager;
    private DetectionManager detectionManager;
    private NotificationManager notificationManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // 初始化配置管理器
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        // 初始化檢測管理器
        detectionManager = new DetectionManager(this);
        
        // 初始化通知管理器
        notificationManager = new NotificationManager(this);
        
        // 註冊事件監聽器
        getServer().getPluginManager().registerEvents(new MiningListener(this), this);
        
        // 註冊命令
        getCommand("anticheat").setExecutor(new AntiCheatCommand(this));
        
        getLogger().info("AntiCheat 插件已啟用！版本: " + getDescription().getVersion());
        
        // 啟動檢測任務
        startDetectionTasks();
    }
    
    @Override
    public void onDisable() {
        if (detectionManager != null) {
            detectionManager.shutdown();
        }
        
        getLogger().info("AntiCheat 插件已停用！");
    }
    
    /**
     * 啟動檢測任務
     */
    private void startDetectionTasks() {
        if (!configManager.isEnabled()) {
            getLogger().warning("AntiCheat 在配置中被禁用！");
            return;
        }
        
        long interval = configManager.getCheckInterval();
        
        // 啟動挖礦軌跡檢測任務
        if (configManager.isMiningTraceEnabled()) {
            getServer().getScheduler().runTaskTimerAsynchronously(this, 
                detectionManager::checkMiningTraces, 20L, interval / 50L);
        }
        
        // 啟動挖礦速度檢測任務
        if (configManager.isMiningSpeedEnabled()) {
            getServer().getScheduler().runTaskTimerAsynchronously(this, 
                detectionManager::checkMiningSpeeds, 20L, interval / 50L);
        }
        
        getLogger().info("檢測任務已啟動，間隔: " + interval + "ms");
    }
    
    /**
     * 重新載入插件
     */
    public void reload() {
        try {
            configManager.loadConfig();
            detectionManager.reload();
            notificationManager.reload();
            
            // 重新啟動檢測任務
            getServer().getScheduler().cancelTasks(this);
            startDetectionTasks();
            
            getLogger().info("AntiCheat 配置已重新載入！");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "重新載入配置時發生錯誤", e);
        }
    }
    
    // Getter 方法
    public static AntiCheat getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DetectionManager getDetectionManager() {
        return detectionManager;
    }
    
    public NotificationManager getNotificationManager() {
        return notificationManager;
    }
}
