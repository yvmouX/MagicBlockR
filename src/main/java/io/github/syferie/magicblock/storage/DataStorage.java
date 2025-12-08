package io.github.syferie.magicblock.storage;

import java.util.Map;
import java.util.UUID;

/**
 * 数据存储接口 - 统一所有存储实现
 *
 * 设计目的:
 * - 抽象存储层，支持多种存储后端 (YAML, JSON, MySQL)
 * - 消除代码中大量的 if (databaseManager != null) 判断
 * - 使用策略模式，可在运行时切换存储方式
 *
 * 实现类:
 * - JsonDataStorage (JSON文件存储)
 * - DatabaseDataStorage (MySQL数据库存储)
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public interface DataStorage {

    /**
     * 保存方块绑定
     *
     * @param player 玩家UUID
     * @param blockId 方块ID
     * @param material 方块材料
     * @param uses 当前使用次数
     * @param maxUses 最大使用次数
     */
    void saveBinding(UUID player, String blockId, String material, int uses, int maxUses);

    /**
     * 更新方块绑定
     *
     * @param player 玩家UUID
     * @param blockId 方块ID
     * @param material 方块材料
     * @param uses 当前使用次数
     * @param maxUses 最大使用次数
     */
    void updateBinding(UUID player, String blockId, String material, int uses, int maxUses);

    /**
     * 删除方块绑定
     *
     * @param player 玩家UUID
     * @param blockId 方块ID
     */
    void deleteBinding(UUID player, String blockId);

    /**
     * 获取玩家的所有绑定
     *
     * @param player 玩家UUID
     * @return 绑定数据 Map<blockId, Map<"material"|"uses"|"maxUses", Object>>
     */
    Map<String, Map<String, Object>> getPlayerBindings(UUID player);

    /**
     * 获取所有玩家的绑定数据
     *
     * @return 所有绑定数据 Map<playerUUID, Map<blockId, binding>>
     */
    Map<UUID, Map<String, Map<String, Object>>> getAllBindings();

    /**
     * 删除玩家的所有绑定
     *
     * @param player 玩家UUID
     */
    void deletePlayerBindings(UUID player);

    /**
     * 保存收藏数据
     *
     * @param player 玩家UUID
     * @param favorites 收藏的材料集合
     */
    void saveFavorites(UUID player, java.util.Set<org.bukkit.Material> favorites);

    /**
     * 加载收藏数据
     *
     * @param player 玩家UUID
     * @return 收藏的材料集合
     */
    java.util.Set<org.bukkit.Material> loadFavorites(UUID player);

    /**
     * 关闭存储连接
     */
    void close();

    /**
     * 检查存储是否可用
     *
     * @return 如果存储可用返回true
     */
    boolean isEnabled();
}
