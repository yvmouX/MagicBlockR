package io.github.syferie.magicblock.listener.handlers;

import io.github.syferie.magicblock.MagicBlockPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;

/**
 * 实体方块变化监听器
 *
 * 职责：
 * - 防止末影人搬运魔法方块
 * - 防止凋灵破坏魔法方块
 * - 防止羊吃草破坏魔法方块
 *
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public class BlockEntityChangeHandler extends BaseListener {

    public BlockEntityChangeHandler(MagicBlockPlugin plugin) {
        super(plugin);
    }

    /**
     * 防止实体改变魔法方块
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();

        // 如果是魔法方块，取消实体的方块改变
        if (indexManager.isMagicBlock(location)) {
            event.setCancelled(true);
            debug("防止实体 " + event.getEntity().getType() + " 改变魔法方块");
        }
    }
}
