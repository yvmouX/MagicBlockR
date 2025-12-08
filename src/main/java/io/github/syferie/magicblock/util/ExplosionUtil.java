package io.github.syferie.magicblock.util;

import com.tcoded.folialib.FoliaLib;
import io.github.syferie.magicblock.MagicBlockPlugin;
import io.github.syferie.magicblock.manager.MagicBlockIndexManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * 爆炸处理工具类 - 统一管理魔法方块在爆炸事件中的处理逻辑
 *
 * 用途：
 * - 处理实体爆炸（苦力怕、TNT等）
 * - 处理方块爆炸（床、重生锚等）
 * - 防止魔法方块被爆炸破坏产生掉落物
 * - 特殊处理红石组件
 *
 */
public final class ExplosionUtil {

    private ExplosionUtil() {
        // 工具类不允许实例化
    }

    /**
     * 处理爆炸事件中的魔法方块
     *
     * @param blocks 爆炸影响的方块列表
     * @param plugin 插件实例
     * @param foliaLib FoliaLib调度器
     * @return 需要保留（不被爆炸破坏）的方块列表
     */
    public static List<Block> handleExplosion(List<Block> blocks, MagicBlockPlugin plugin, FoliaLib foliaLib) {
        List<Block> blocksToKeep = new ArrayList<>();
        MagicBlockIndexManager indexManager = plugin.getIndexManager();

        for (Block block : blocks) {
            if (indexManager.isMagicBlock(block.getLocation())) {
                blocksToKeep.add(block);
                handleMagicBlockInExplosion(block, indexManager, foliaLib);
            }
        }

        return blocksToKeep;
    }

    /**
     * 处理爆炸中的单个魔法方块
     *
     * @param block 方块
     * @param indexManager 索引管理器
     * @param foliaLib Folia调度器
     */
    private static void handleMagicBlockInExplosion(Block block, MagicBlockIndexManager indexManager, FoliaLib foliaLib) {
        Material blockType = block.getType();
        final Location blockLocation = block.getLocation();

        // 对于红石组件类方块，需要特别处理
        if (isRedstoneComponent(blockType)) {
            // 立即设置为空气，防止掉落物生成
            block.setType(Material.AIR);

            // 然后延迟移除记录
            foliaLib.getScheduler().runLater(() -> {
                indexManager.unregisterMagicBlock(blockLocation);
            }, 1L);
        } else {
            // 对于其他类型的方块，使用标准处理方式
            foliaLib.getScheduler().runLater(() -> {
                if (indexManager.isMagicBlock(blockLocation)) {
                    block.setType(Material.AIR);
                    indexManager.unregisterMagicBlock(blockLocation);
                }
            }, 1L);
        }
    }

    /**
     * 检查是否是红石组件
     *
     * 红石组件需要特殊处理，因为它们在被破坏时会自动掉落物品
     *
     * @param material 方块材料
     * @return 如果是红石组件返回true
     */
    public static boolean isRedstoneComponent(Material material) {
        switch (material) {
            case LEVER:
            case STONE_BUTTON:
            case OAK_BUTTON:
            case SPRUCE_BUTTON:
            case BIRCH_BUTTON:
            case JUNGLE_BUTTON:
            case ACACIA_BUTTON:
            case DARK_OAK_BUTTON:
            case CRIMSON_BUTTON:
            case WARPED_BUTTON:
            // 以下材料类型在 1.19+ 版本才可用，暂时注释以兼容 1.18.2
            // case MANGROVE_BUTTON:
            // case CHERRY_BUTTON:
            // case BAMBOO_BUTTON:
            case STONE_PRESSURE_PLATE:
            case OAK_PRESSURE_PLATE:
            case SPRUCE_PRESSURE_PLATE:
            case BIRCH_PRESSURE_PLATE:
            case JUNGLE_PRESSURE_PLATE:
            case ACACIA_PRESSURE_PLATE:
            case DARK_OAK_PRESSURE_PLATE:
            case CRIMSON_PRESSURE_PLATE:
            case WARPED_PRESSURE_PLATE:
            // 以下材料类型在 1.19+ 版本才可用，暂时注释以兼容 1.18.2
            // case MANGROVE_PRESSURE_PLATE:
            // case CHERRY_PRESSURE_PLATE:
            // case BAMBOO_PRESSURE_PLATE:
            case LIGHT_WEIGHTED_PRESSURE_PLATE:
            case HEAVY_WEIGHTED_PRESSURE_PLATE:
            case TRIPWIRE_HOOK:
            case TRIPWIRE:
            case REDSTONE_TORCH:
            case REDSTONE_WALL_TORCH:
            case REDSTONE_WIRE:
            case REPEATER:
            case COMPARATOR:
            case OBSERVER:
            case LECTERN:
            case TARGET:
            case LIGHTNING_ROD:
            case DAYLIGHT_DETECTOR:
            case REDSTONE_LAMP:
            case PISTON:
            case STICKY_PISTON:
            case DISPENSER:
            case DROPPER:
            case HOPPER:
            case NOTE_BLOCK:
            case JUKEBOX:
                return true;
            default:
                return false;
        }
    }
}
