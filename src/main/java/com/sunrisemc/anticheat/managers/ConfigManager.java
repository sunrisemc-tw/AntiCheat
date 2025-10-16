package com.sunrisemc.anticheat.managers;

import com.sunrisemc.anticheat.AntiCheat;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置管理器
 * 負責管理插件的所有配置選項
 */
public class ConfigManager {
    
    private final AntiCheat plugin;
    private FileConfiguration config;
    
    public ConfigManager(AntiCheat plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 載入配置文件
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
    
    // 一般設定
    public boolean isEnabled() {
        return config.getBoolean("general.enabled", true);
    }
    
    public long getCheckInterval() {
        return config.getLong("general.check-interval", 1000L);
    }
    
    public int getMaxCheckDistance() {
        return config.getInt("general.max-check-distance", 10);
    }
    
    public boolean isDebugEnabled() {
        return config.getBoolean("general.debug", false);
    }
    
    // 挖礦軌跡檢測設定
    public boolean isMiningTraceEnabled() {
        return config.getBoolean("mining-trace.enabled", true);
    }
    
    public int getChunkRadius() {
        return config.getInt("mining-trace.chunk-radius", 2);
    }
    
    public int getMaxTraceLength() {
        return config.getInt("mining-trace.max-trace-length", 50);
    }
    
    public int getTraceSensitivity() {
        return config.getInt("mining-trace.sensitivity", 5);
    }
    
    public long getTraceCooldown() {
        return config.getLong("mining-trace.cooldown", 2000L);
    }
    
    // 挖礦速度檢測設定
    public boolean isMiningSpeedEnabled() {
        return config.getBoolean("mining-speed.enabled", true);
    }
    
    public double getNormalSpeedMultiplier() {
        return config.getDouble("mining-speed.normal-speed-multiplier", 1.5);
    }
    
    public long getDetectionWindow() {
        return config.getLong("mining-speed.detection-window", 5000L);
    }
    
    public int getMinDetectionCount() {
        return config.getInt("mining-speed.min-detection-count", 3);
    }
    
    public Map<Material, Long> getToolSpeeds() {
        Map<Material, Long> toolSpeeds = new HashMap<>();
        
        if (config.contains("mining-speed.tool-speeds")) {
            for (String toolName : config.getConfigurationSection("mining-speed.tool-speeds").getKeys(false)) {
                try {
                    Material material = Material.valueOf(toolName.toUpperCase());
                    long speed = config.getLong("mining-speed.tool-speeds." + toolName);
                    toolSpeeds.put(material, speed);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("無效的工具材料: " + toolName);
                }
            }
        }
        
        return toolSpeeds;
    }
    
    // 通知設定
    public boolean isNotificationEnabled() {
        return config.getBoolean("notifications.enabled", true);
    }
    
    public long getNotificationCooldown() {
        return config.getLong("notifications.cooldown", 10000L);
    }
    
    public boolean isConsoleNotificationEnabled() {
        return config.getBoolean("notifications.console-notification", true);
    }
    
    public boolean isInGameNotificationEnabled() {
        return config.getBoolean("notifications.in-game-notification", true);
    }
    
    public int getNotificationRadius() {
        return config.getInt("notifications.notification-radius", 5);
    }
    
    // Webhook 設定 (預留)
    public boolean isWebhookEnabled() {
        return config.getBoolean("notifications.webhook.enabled", false);
    }
    
    public String getWebhookUrl() {
        return config.getString("notifications.webhook.url", "");
    }
    
    public int getWebhookTimeout() {
        return config.getInt("notifications.webhook.timeout", 5000);
    }
    
    // 懲罰設定
    public boolean isPunishmentEnabled() {
        return config.getBoolean("punishments.enabled", false);
    }
    
    public String getPunishmentType() {
        return config.getString("punishments.type", "WARN");
    }
    
    public String getPunishmentMessage() {
        return config.getString("punishments.message", "檢測到異常挖礦行為，若您認為是誤判，請聯繫管理員");
    }
    
    public int getWarningCount() {
        return config.getInt("punishments.warning-count", 3);
    }
    
    // 白名單設定
    public boolean isWhitelistEnabled() {
        return config.getBoolean("whitelist.enabled", false);
    }
    
    public List<String> getWhitelistedPlayers() {
        return config.getStringList("whitelist.players");
    }
    
    public List<String> getWhitelistedWorlds() {
        return config.getStringList("whitelist.worlds");
    }
}

