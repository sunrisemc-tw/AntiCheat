package com.sunrisemc.anticheat.listeners;

import com.sunrisemc.anticheat.AntiCheat;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

/**
 * 挖礦事件監聽器
 * 監聽玩家挖礦事件並記錄數據
 */
public class MiningListener implements Listener {
    
    private final AntiCheat plugin;
    
    public MiningListener(AntiCheat plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        // 檢查插件是否啟用
        if (!plugin.getConfigManager().isEnabled()) {
            return;
        }
        
        // 檢查玩家權限
        if (player.hasPermission("anticheat.bypass")) {
            return;
        }
        
        // 檢查白名單
        if (isPlayerWhitelisted(player)) {
            return;
        }
        
        // 獲取玩家手中的工具
        ItemStack tool = player.getInventory().getItemInMainHand();
        Material toolType = tool.getType();
        
        // 只監聽使用工具的挖礦事件
        if (!isMiningTool(toolType)) {
            return;
        }
        
        // 記錄挖礦事件
        plugin.getDetectionManager().recordMiningEvent(
            player,
            event.getBlock().getLocation(),
            event.getBlock().getType(),
            toolType
        );
        
        // 調試信息
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info(String.format("記錄挖礦事件: %s 挖掘 %s 使用 %s 在 %s", 
                player.getName(),
                event.getBlock().getType(),
                toolType,
                event.getBlock().getLocation()));
        }
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
    
    /**
     * 檢查是否為挖礦工具
     */
    private boolean isMiningTool(Material material) {
        switch (material) {
            case WOODEN_PICKAXE:
            case STONE_PICKAXE:
            case IRON_PICKAXE:
            case GOLDEN_PICKAXE:
            case DIAMOND_PICKAXE:
            case NETHERITE_PICKAXE:
            case WOODEN_SHOVEL:
            case STONE_SHOVEL:
            case IRON_SHOVEL:
            case GOLDEN_SHOVEL:
            case DIAMOND_SHOVEL:
            case NETHERITE_SHOVEL:
            case WOODEN_AXE:
            case STONE_AXE:
            case IRON_AXE:
            case GOLDEN_AXE:
            case DIAMOND_AXE:
            case NETHERITE_AXE:
                return true;
            default:
                return false;
        }
    }
}
