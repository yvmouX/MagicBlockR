package io.github.syferie.magicblock.listener.handlers;

import io.github.syferie.magicblock.MagicBlockPlugin;
import io.github.syferie.magicblock.gui.GUIManager;
import io.github.syferie.magicblock.util.ConnectionBlockUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * 方块放置监听器
 *
 * 职责：
 * - 处理魔法方块的放置事件
 * - 检查权限和使用次数
 * - 处理方块绑定
 * - 更新连接型方块状态
 * - 记录使用统计
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public class BlockPlacementHandler extends BaseListener {

    public BlockPlacementHandler(MagicBlockPlugin plugin) {
        super(plugin);
    }

    /**
     * 处理单方块放置
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // BlockMultiPlaceEvent 由专门的处理器处理
        if (event instanceof BlockMultiPlaceEvent) {
            return;
        }

        ItemStack item = event.getItemInHand();
        if (plugin.hasMagicLore(item.getItemMeta())) {
            handleMagicBlockPlace(event, item);
        }
    }

    /**
     * 处理多方块放置 (如床、门)
     */
    @EventHandler
    public void onBlockMultiPlace(BlockMultiPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (plugin.hasMagicLore(item.getItemMeta())) {
            handleMagicBlockPlace(event, item);
        }
    }

    /**
     * 处理魔法方块放置的核心逻辑
     */
    private void handleMagicBlockPlace(BlockPlaceEvent event, ItemStack item) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        // 1. 检查使用权限
        if (!player.hasPermission("magicblock.use")) {
            event.setCancelled(true);
            plugin.sendMessage(player, "messages.no-permission-use");
            return;
        }

        // 2. 检查使用次数
        int useTimes = blockManager.getUseTimes(item);
        if (useTimes <= 0) {
            event.setCancelled(true);
            handleDepletedBlock(player, item);
            return;
        }

        // 3. 处理方块绑定系统
        if (!handleBinding(player, item, event)) {
            return; // 绑定检查失败，事件已取消
        }

        // 4. 注册魔法方块到索引系统
        indexManager.registerMagicBlock(block.getLocation(), item);

        // 5. 更新连接型方块的视觉连接状态
        updateBlockConnections(block, item.getType());

        // 6. 减少使用次数
        if (useTimes > 0) { // -1 表示无限使用
            blockManager.decrementUseTimes(item);
        }

        // 7. 记录使用统计
        plugin.incrementPlayerUsage(player.getUniqueId());
        plugin.logUsage(player, item);
    }

    /**
     * 处理耗尽的方块
     */
    private void handleDepletedBlock(Player player, ItemStack item) {
        String message = plugin.getLanguageManager().getMessage("messages.block-removed");
        String prefix = plugin.getLanguageManager().getMessage("general.prefix");
        player.sendMessage(prefix + message);

        // 处理耗尽的方块绑定
        plugin.getBlockBindManager().handleDepleted(item);

        // 如果配置为移除耗尽的方块 (使用配置缓存)
        if (plugin.getConfigCache().isRemoveDepletedBlocks()) {
            player.getInventory().setItemInMainHand(null);
        }
    }

    /**
     * 处理方块绑定逻辑
     *
     * @return false 表示绑定检查失败，应取消事件
     */
    private boolean handleBinding(Player player, ItemStack item, BlockPlaceEvent event) {
        // 使用配置缓存替代直接读取
        boolean bindingEnabled = plugin.getConfigCache().isBindingSystemEnabled();
        UUID boundPlayer = plugin.getBlockBindManager().getBoundPlayer(item);

        if (bindingEnabled && boundPlayer == null) {
            // 第一次使用时自动绑定
            plugin.getBlockBindManager().bindBlock(player, item);
        } else if (boundPlayer != null && !boundPlayer.equals(player.getUniqueId())) {
            // 检查是否允许使用已绑定的方块 (使用配置缓存)
            if (!plugin.getConfigCache().isAllowUseBoundBlocks()) {
                event.setCancelled(true);
                plugin.sendMessage(player, "messages.not-bound-to-you");
                return false;
            }
        }

        return true;
    }

    /**
     * 更新连接型方块的连接状态
     */
    private void updateBlockConnections(Block block, Material material) {
        if (ConnectionBlockUtil.isConnectableBlock(material) &&
            ConnectionBlockUtil.hasAdjacentConnectableBlocks(block)) {
            // 延迟1tick更新连接状态，确保方块已完全放置
            foliaLib.getScheduler().runLater(() -> {
                ConnectionBlockUtil.updateConnectedBlocks(block);
            }, 1L);
        }
    }
}
