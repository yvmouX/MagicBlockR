package io.github.syferie.magicblock.listener.handlers;

import io.github.syferie.magicblock.MagicBlockPlugin;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockRedstoneEvent;

/**
 * 红石信号监听器
 *
 * 职责：
 * - 确保魔法方块位置上的红石组件可以正常接收红石信号
 * - 更新红石组件的状态
 *
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public class BlockRedstoneHandler extends BaseListener {

    private static final BlockFace[] ALL_FACES = {
        BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN
    };

    public BlockRedstoneHandler(MagicBlockPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockRedstone(BlockRedstoneEvent event) {
        Block block = event.getBlock();

        // 检查周围的红石组件
        for (BlockFace face : ALL_FACES) {
            Block adjacent = block.getRelative(face);
            Material type = adjacent.getType();

            // 如果是魔法方块位置上的红石组件，确保它们可以接收红石信号
            if (indexManager.isMagicBlock(adjacent.getLocation()) && isRedstoneComponent(type)) {
                // 对于需要状态更新的方块，延迟更新
                if (needsStateUpdate(type)) {
                    final Block targetBlock = adjacent;
                    foliaLib.getScheduler().runLater(() -> {
                        targetBlock.getState().update(true, true);
                    }, 1L);
                }
            }
        }

        // 如果当前方块本身是魔法方块位置上的红石组件，允许它正常工作
        if (indexManager.isMagicBlock(block.getLocation()) && isRedstoneComponent(block.getType())) {
            // 不取消事件，允许红石信号传递
        }
    }

    /**
     * 检查方块是否需要状态更新
     */
    private boolean needsStateUpdate(Material type) {
        return type == Material.REDSTONE_LAMP ||
               type == Material.DISPENSER ||
               type == Material.DROPPER ||
               type == Material.HOPPER ||
               type == Material.PISTON ||
               type == Material.STICKY_PISTON ||
               type == Material.OBSERVER ||
               type == Material.NOTE_BLOCK ||
               type == Material.POWERED_RAIL ||
               type == Material.DETECTOR_RAIL ||
               type == Material.ACTIVATOR_RAIL;
    }

    /**
     * 检查是否是红石组件
     */
    private boolean isRedstoneComponent(Material type) {
        return io.github.syferie.magicblock.util.ExplosionUtil.isRedstoneComponent(type);
    }
}
