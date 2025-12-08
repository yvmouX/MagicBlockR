package io.github.syferie.magicblock.listener.handlers;

import com.tcoded.folialib.FoliaLib;
import io.github.syferie.magicblock.MagicBlockPlugin;
import io.github.syferie.magicblock.block.BlockManager;
import io.github.syferie.magicblock.manager.MagicBlockIndexManager;
import org.bukkit.event.Listener;

/**
 * 事件监听器基类
 *
 * 提供所有专职监听器需要的公共依赖和工具方法
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public abstract class BaseListener implements Listener {

    protected final MagicBlockPlugin plugin;
    protected final BlockManager blockManager;
    protected final MagicBlockIndexManager indexManager;
    protected final FoliaLib foliaLib;

    public BaseListener(MagicBlockPlugin plugin) {
        this.plugin = plugin;
        this.blockManager = plugin.getBlockManager();
        this.indexManager = plugin.getIndexManager();
        this.foliaLib = plugin.getFoliaLib();
    }

    /**
     * 检查世界是否在黑名单中
     */
    protected boolean isWorldBlacklisted(String worldName) {
        return plugin.getBlacklistedWorlds().contains(worldName);
    }

    /**
     * 输出调试日志
     */
    protected void debug(String message) {
        plugin.debug(message);
    }
}
