package io.github.syferie.magicblock.core;

import io.github.syferie.magicblock.MagicBlockPlugin;
import io.github.syferie.magicblock.api.IMagicItem;
import io.github.syferie.magicblock.util.LoreUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 魔法物品抽象基类
 *
 * 职责:
 * 1. 使用次数管理 (setUseTimes, getUseTimes, decrementUseTimes)
 * 2. 最大使用次数管理 (setMaxUseTimes, getMaxUseTimes)
 * 3. Lore 统一生成和缓存
 * 4. PDC 数据持久化
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public abstract class AbstractMagicItem implements IMagicItem {
    protected final MagicBlockPlugin plugin;
    protected final NamespacedKey useTimesKey;
    protected final NamespacedKey maxTimesKey;

    // 无限使用次数标识
    protected static final int INFINITE_USES = Integer.MAX_VALUE - 100;

    /**
     * 构造函数
     *
     * @param plugin 插件实例
     * @param keyPrefix PDC Key前缀 (如 "magicblock" 或 "magicfood")
     */
    public AbstractMagicItem(MagicBlockPlugin plugin, String keyPrefix) {
        this.plugin = plugin;
        // 🔧 修复: 使用旧的Key格式以保持兼容性
        this.useTimesKey = new NamespacedKey(plugin, keyPrefix + "_usetimes");
        this.maxTimesKey = new NamespacedKey(plugin, keyPrefix + "_maxtimes");
    }

    // ==================== 模板方法 - 子类必须实现 ====================

    /**
     * 获取魔法标识lore (如 "§e⚡ §7MagicBlock" 或 "§7MagicFood")
     */
    protected abstract String getMagicLoreIdentifier();

    /**
     * 获取装饰性lore列表
     *
     * @param item 物品
     * @param owner 物品所有者 (可能为null)
     * @return 装饰性lore列表
     */
    protected abstract List<String> getDecorativeLore(ItemStack item, Player owner);

    /**
     * 获取使用次数前缀文本 (如 "Total times:" 或 "Uses:")
     */
    protected abstract String getUsageLorePrefix();

    /**
     * 是否应该显示绑定信息
     *
     * @return true表示显示绑定信息 (方块), false表示不显示 (食物)
     */
    protected boolean shouldShowBinding() {
        return false; // 默认不显示，方块会覆盖为true
    }

    // ==================== 统一实现的方法 ====================

    @Override
    public void setUseTimes(ItemStack item, int times) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (times == -1) {
            // 无限使用
            container.set(useTimesKey, PersistentDataType.INTEGER, INFINITE_USES);
            container.set(maxTimesKey, PersistentDataType.INTEGER, INFINITE_USES);
        } else {
            container.set(useTimesKey, PersistentDataType.INTEGER, times);

            // 如果最大使用次数未设置，则同时设置
            if (!container.has(maxTimesKey, PersistentDataType.INTEGER)) {
                container.set(maxTimesKey, PersistentDataType.INTEGER, times);
            }
        }

        item.setItemMeta(meta);
    }

    @Override
    public int getUseTimes(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;

        return meta.getPersistentDataContainer()
            .getOrDefault(useTimesKey, PersistentDataType.INTEGER, 0);
    }

    @Override
    public int decrementUseTimes(ItemStack item) {
        int currentTimes = getUseTimes(item);
        if (currentTimes <= 0) return 0;

        currentTimes--;

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer()
                .set(useTimesKey, PersistentDataType.INTEGER, currentTimes);
            item.setItemMeta(meta);
        }

        // 更新lore
        updateLore(item, currentTimes);

        return currentTimes;
    }

    @Override
    public void setMaxUseTimes(ItemStack item, int maxTimes) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        int actualMaxTimes = maxTimes == -1 ? INFINITE_USES : maxTimes;
        meta.getPersistentDataContainer()
            .set(maxTimesKey, PersistentDataType.INTEGER, actualMaxTimes);
        item.setItemMeta(meta);
    }

    @Override
    public int getMaxUseTimes(ItemStack item) {
        if (!isMagicItem(item)) return 0;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;

        Integer maxTimes = meta.getPersistentDataContainer()
            .get(maxTimesKey, PersistentDataType.INTEGER);

        // 如果未设置，使用默认值
        if (maxTimes == null) {
            maxTimes = plugin.getDefaultBlockTimes();
            setMaxUseTimes(item, maxTimes);
        }

        return maxTimes;
    }

    @Override
    public void updateLore(ItemStack item, int remainingTimes) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        int maxTimes = getMaxUseTimes(item);
        if (maxTimes <= 0) return;

        boolean isInfinite = LoreUtil.isInfiniteUses(maxTimes);

        // 直接构建lore（无缓存，代码简洁）
        Player owner = getOwnerPlayer(item);
        List<String> lore = buildLore(item, owner, remainingTimes, maxTimes, isInfinite);

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    // ==================== 辅助方法 ====================

    /**
     * 构建完整的lore
     */
    private List<String> buildLore(ItemStack item, Player owner,
                                    int remainingTimes, int maxTimes, boolean isInfinite) {
        List<String> lore = new ArrayList<>();

        // 1. 魔法标识
        lore.add(getMagicLoreIdentifier());

        // 2. 装饰性lore (使用配置缓存)
        if (plugin.getConfigCache().isDecorativeLoreEnabled()) {
            lore.addAll(getDecorativeLore(item, owner));
        }

        // 3. 绑定信息 (如果需要，使用配置缓存)
        if (shouldShowBinding()) {
            UUID boundPlayer = getBoundPlayer(item);
            if (boundPlayer != null && plugin.getConfigCache().isShowBoundPlayer()) {
                String bindLore = LoreUtil.generateBindingLore(
                    getBindingLorePrefix(), boundPlayer);
                if (bindLore != null) {
                    lore.add(bindLore);
                }
            }
        }

        // 4. 使用次数 (使用配置缓存)
        if (plugin.getConfigCache().isShowUsageCount()) {
            lore.add(LoreUtil.generateUsageText(
                getUsageLorePrefix(), remainingTimes, maxTimes, isInfinite));
        }

        // 5. 进度条 (非无限次数时，使用配置缓存)
        if (!isInfinite && plugin.getConfigCache().isShowProgressBar()) {
            lore.add(LoreUtil.generateProgressBar(remainingTimes, maxTimes, 10));
        }

        return lore;
    }

    /**
     * 获取物品所有者
     */
    protected Player getOwnerPlayer(ItemStack item) {
        if (!shouldShowBinding()) return null;

        UUID boundPlayer = getBoundPlayer(item);
        if (boundPlayer == null) return null;

        return plugin.getServer().getPlayer(boundPlayer);
    }

    /**
     * 获取绑定的玩家UUID
     */
    protected UUID getBoundPlayer(ItemStack item) {
        if (!shouldShowBinding()) return null;
        return plugin.getBlockBindManager().getBoundPlayer(item);
    }

    /**
     * 获取绑定lore前缀
     */
    protected String getBindingLorePrefix() {
        return plugin.getBlockBindManager().getBindLorePrefix();
    }
}
