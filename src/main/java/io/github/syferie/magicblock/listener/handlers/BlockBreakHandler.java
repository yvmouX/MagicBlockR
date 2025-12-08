package io.github.syferie.magicblock.listener.handlers;

import io.github.syferie.magicblock.MagicBlockPlugin;
import io.github.syferie.magicblock.util.ConnectionBlockUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 方块破坏监听器
 *
 * 职责：
 * - 处理魔法方块的破坏事件
 * - 检查破坏权限和绑定
 * - 更新连接型方块状态
 * - 清理方块索引和绑定数据
 *
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public class BlockBreakHandler extends BaseListener {

    private static final BlockFace[] HORIZONTAL_FACES = {
        BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
    };

    public BlockBreakHandler(MagicBlockPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();
        Player player = event.getPlayer();

        // 使用索引系统进行 O(1) 查找
        boolean isMagicBlock = indexManager.isMagicBlock(location);

        // 1. 更新连接型方块的相邻方块
        updateAdjacentConnections(block);

        // 2. 处理魔法方块破坏
        if (isMagicBlock) {
            handleMagicBlockBreak(event, block, player, location);
        }
    }

    /**
     * 更新相邻连接方块的视觉状态
     */
    private void updateAdjacentConnections(Block block) {
        if (!ConnectionBlockUtil.isConnectableBlock(block.getType())) {
            return;
        }

        // 收集相邻的同类型方块
        final List<Block> adjacentBlocks = new ArrayList<>();
        for (BlockFace face : HORIZONTAL_FACES) {
            Block adjacent = block.getRelative(face);
            if (ConnectionBlockUtil.isConnectableBlock(adjacent.getType())) {
                adjacentBlocks.add(adjacent);
            }
        }

        // 延迟更新连接状态
        if (!adjacentBlocks.isEmpty()) {
            foliaLib.getScheduler().runLater(() -> {
                for (Block adjacent : adjacentBlocks) {
                    ConnectionBlockUtil.updateAdjacentBlockConnections(adjacent);
                }
            }, 1L);
        }
    }

    /**
     * 处理魔法方块破坏的核心逻辑
     */
    private void handleMagicBlockBreak(BlockBreakEvent event, Block block, Player player, Location location) {
        // 1. 检查破坏权限
        if (!player.hasPermission("magicblock.break")) {
            event.setCancelled(true);
            plugin.sendMessage(player, "messages.no-permission-break");
            return;
        }

        ItemStack blockItem = new ItemStack(block.getType());

        // 2. 检查绑定权限
        if (!checkBindingPermission(player, blockItem, event)) {
            return; // 绑定检查失败，事件已取消
        }

        // 3. 禁止掉落和经验
        event.setDropItems(false);
        event.setExpToDrop(0);

        // 4. 清理绑定数据
        plugin.getBlockBindManager().cleanupBindings(blockItem);

        // 5. 延迟清理索引（确保方块确实被破坏）
        cleanupBlockIndex(location);
    }

    /**
     * 检查绑定权限
     *
     * @return false 表示权限检查失败，应取消事件
     */
    private boolean checkBindingPermission(Player player, ItemStack blockItem, BlockBreakEvent event) {
        // 使用配置缓存
        boolean bindingEnabled = plugin.getConfigCache().isBindingSystemEnabled();

        if (bindingEnabled && plugin.getBlockBindManager().isBlockBound(blockItem)) {
            UUID boundPlayer = plugin.getBlockBindManager().getBoundPlayer(blockItem);
            if (boundPlayer != null && !boundPlayer.equals(player.getUniqueId())) {
                event.setCancelled(true);
                plugin.sendMessage(player, "messages.not-bound-to-you");
                return false;
            }
        }

        return true;
    }

    /**
     * 清理方块索引
     *
     * 延迟执行以确保方块确实被破坏（防止被其他插件如Residence替换）
     */
    private void cleanupBlockIndex(Location location) {
        foliaLib.getScheduler().runAtLocationLater(location, () -> {
            Block blockAtLocation = location.getBlock();
            if (blockAtLocation.getType() == Material.AIR) {
                indexManager.unregisterMagicBlock(location);
            }
        }, 1L);
    }
}
