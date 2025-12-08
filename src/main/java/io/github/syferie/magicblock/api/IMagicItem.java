package io.github.syferie.magicblock.api;

import org.bukkit.inventory.ItemStack;

/**
 * 魔法物品统一接口
 *
 * 设计目的:
 * - 统一 IMagicBlock 和 IMagicFood 的公共方法
 * - 为所有魔法物品类型提供一致的API
 *
 * 实现类:
 * - AbstractMagicItem (抽象基类)
 *   ├── BlockManager (方块实现)
 *   └── FoodManager (食物实现)
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public interface IMagicItem {

    // ==================== 使用次数管理 ====================

    /**
     * 设置物品的使用次数
     *
     * @param item 物品
     * @param times 次数 (-1 表示无限使用)
     */
    void setUseTimes(ItemStack item, int times);

    /**
     * 获取物品的剩余使用次数
     *
     * @param item 物品
     * @return 剩余使用次数 (0 表示已耗尽)
     */
    int getUseTimes(ItemStack item);

    /**
     * 减少物品的使用次数
     *
     * @param item 物品
     * @return 剩余使用次数
     */
    int decrementUseTimes(ItemStack item);

    // ==================== 最大使用次数管理 ====================

    /**
     * 设置物品的最大使用次数
     *
     * @param item 物品
     * @param maxTimes 最大次数 (-1 表示无限)
     */
    void setMaxUseTimes(ItemStack item, int maxTimes);

    /**
     * 获取物品的最大使用次数
     *
     * @param item 物品
     * @return 最大使用次数
     */
    int getMaxUseTimes(ItemStack item);

    // ==================== Lore 管理 ====================

    /**
     * 更新物品的lore显示
     *
     * @param item 物品
     * @param remainingTimes 剩余使用次数
     */
    void updateLore(ItemStack item, int remainingTimes);

    // ==================== 物品识别 ====================

    /**
     * 检查是否是魔法物品
     *
     * @param item 物品
     * @return 如果是魔法物品返回true
     */
    boolean isMagicItem(ItemStack item);

    /**
     * 获取魔法物品类型标识
     *
     * @return 类型字符串 (如 "BLOCK", "FOOD")
     */
    String getMagicItemType();
}
