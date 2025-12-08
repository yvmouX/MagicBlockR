package io.github.syferie.magicblock.listener.handlers;

import io.github.syferie.magicblock.MagicBlockPlugin;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

/**
 * 方块爆炸监听器
 *
 * 职责：
 * - 防止魔法方块被爆炸破坏
 * - 处理苦力怕、TNT等实体爆炸
 * - 处理床、重生锚等方块爆炸
 *
 *
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public class BlockExplosionHandler extends BaseListener {

    public BlockExplosionHandler(MagicBlockPlugin plugin) {
        super(plugin);
    }

    /**
     * 处理实体爆炸 (苦力怕、TNT、末影水晶等)
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        List<Block> blocksToKeep = io.github.syferie.magicblock.util.ExplosionUtil.handleExplosion(
                event.blockList(), plugin, foliaLib);

        // 从爆炸列表中移除魔法方块，防止它们被爆炸破坏并产生掉落物
        event.blockList().removeAll(blocksToKeep);
    }

    /**
     * 处理方块爆炸 (床、重生锚等)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockExplode(BlockExplodeEvent event) {
        List<Block> blocksToKeep = io.github.syferie.magicblock.util.ExplosionUtil.handleExplosion(
                event.blockList(), plugin, foliaLib);

        // 从爆炸列表中移除魔法方块，防止它们被爆炸破坏并产生掉落物
        event.blockList().removeAll(blocksToKeep);
    }
}
