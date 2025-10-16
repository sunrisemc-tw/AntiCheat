package com.sunrisemc.anticheat.listeners;

import com.sunrisemc.anticheat.AntiCheat;
import com.sunrisemc.anticheat.data.PlayerData;
import com.sunrisemc.anticheat.detection.FlightDetector;
import com.sunrisemc.anticheat.detection.SpeedDetector;
import com.sunrisemc.anticheat.detection.NoClipDetector;
import com.sunrisemc.anticheat.detection.AutoClickDetector;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * 玩家行為監聽器
 * 監聽各種可能的外掛行為
 */
public class PlayerBehaviorListener implements Listener {
    
    private final AntiCheat plugin;
    private final FlightDetector flightDetector;
    private final SpeedDetector speedDetector;
    private final NoClipDetector noClipDetector;
    private final AutoClickDetector autoClickDetector;
    
    public PlayerBehaviorListener(AntiCheat plugin) {
        this.plugin = plugin;
        this.flightDetector = new FlightDetector(plugin);
        this.speedDetector = new SpeedDetector(plugin);
        this.noClipDetector = new NoClipDetector(plugin);
        this.autoClickDetector = new AutoClickDetector(plugin);
    }
    
    /**
     * 監聽玩家移動事件 - 檢測飛行和速度外掛
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        if (!shouldCheckPlayer(player)) {
            return;
        }
        
        PlayerData data = plugin.getDetectionManager().getPlayerData(player.getUniqueId());
        
        // 檢測飛行
        if (plugin.getConfigManager().isFlightDetectionEnabled()) {
            if (flightDetector.detectFlight(player, event.getFrom(), event.getTo(), data)) {
                plugin.getDetectionManager().handleDetection(player, "飛行檢測", "檢測到異常飛行行為");
            }
        }
        
        // 檢測速度
        if (plugin.getConfigManager().isSpeedDetectionEnabled()) {
            if (speedDetector.detectSpeed(player, event.getFrom(), event.getTo(), data)) {
                plugin.getDetectionManager().handleDetection(player, "速度檢測", "檢測到異常移動速度");
            }
        }
        
        // 檢測穿牆
        if (plugin.getConfigManager().isNoClipDetectionEnabled()) {
            if (noClipDetector.detectNoClip(player, event.getFrom(), event.getTo(), data)) {
                plugin.getDetectionManager().handleDetection(player, "穿牆檢測", "檢測到穿牆行為");
            }
        }
    }
    
    /**
     * 監聽玩家切換飛行事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        
        if (!shouldCheckPlayer(player)) {
            return;
        }
        
        PlayerData data = plugin.getDetectionManager().getPlayerData(player.getUniqueId());
        
        if (plugin.getConfigManager().isFlightDetectionEnabled()) {
            if (flightDetector.detectToggleFlight(player, event.isFlying(), data)) {
                plugin.getDetectionManager().handleDetection(player, "飛行切換檢測", "檢測到異常飛行切換");
            }
        }
    }
    
    /**
     * 監聽玩家互動事件 - 檢測自動點擊
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        if (!shouldCheckPlayer(player)) {
            return;
        }
        
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            PlayerData data = plugin.getDetectionManager().getPlayerData(player.getUniqueId());
            
            if (plugin.getConfigManager().isAutoClickDetectionEnabled()) {
                if (autoClickDetector.detectAutoClick(player, data)) {
                    plugin.getDetectionManager().handleDetection(player, "自動點擊檢測", "檢測到異常點擊頻率");
                }
            }
        }
    }
    
    /**
     * 監聽玩家受傷事件 - 檢測無敵外掛
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        if (!shouldCheckPlayer(player)) {
            return;
        }
        
        PlayerData data = plugin.getDetectionManager().getPlayerData(player.getUniqueId());
        
        if (plugin.getConfigManager().isGodModeDetectionEnabled()) {
            if (detectGodMode(player, event, data)) {
                plugin.getDetectionManager().handleDetection(player, "無敵檢測", "檢測到無敵外掛行為");
            }
        }
    }
    
    /**
     * 檢測無敵外掛
     */
    private boolean detectGodMode(Player player, EntityDamageEvent event, PlayerData data) {
        // 記錄受傷事件
        data.addDamageEvent(System.currentTimeMillis(), event.getFinalDamage());
        
        // 檢查是否在短時間內受到大量傷害但沒有死亡
        long currentTime = System.currentTimeMillis();
        long timeWindow = 5000; // 5秒
        
        double totalDamage = data.getRecentDamage(timeWindow);
        
        // 如果受到的傷害超過玩家最大生命值但沒有死亡，可能為無敵外掛
        if (totalDamage > player.getMaxHealth() * 2) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 檢查是否應該檢測該玩家
     */
    private boolean shouldCheckPlayer(Player player) {
        // 檢查插件是否啟用
        if (!plugin.getConfigManager().isEnabled()) {
            return false;
        }
        
        // 檢查玩家權限
        if (player.hasPermission("anticheat.bypass")) {
            return false;
        }
        
        // 檢查創造模式和旁觀者模式
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE || 
            player.getGameMode() == org.bukkit.GameMode.SPECTATOR) {
            return false;
        }
        
        // 檢查白名單
        if (isPlayerWhitelisted(player)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 檢查玩家是否在白名單中
     */
    private boolean isPlayerWhitelisted(Player player) {
        if (!plugin.getConfigManager().isWhitelistEnabled()) {
            return false;
        }
        
        // 檢查玩家白名單
        if (plugin.getConfigManager().getWhitelistedPlayers().contains(player.getName()) ||
            plugin.getConfigManager().getWhitelistedPlayers().contains(player.getUniqueId().toString())) {
            return true;
        }
        
        // 檢查世界白名單
        if (plugin.getConfigManager().getWhitelistedWorlds().contains(player.getWorld().getName())) {
            return true;
        }
        
        return false;
    }
}
