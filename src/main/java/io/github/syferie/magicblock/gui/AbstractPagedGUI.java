package io.github.syferie.magicblock.gui;

import io.github.syferie.magicblock.MagicBlockPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 分页GUI抽象基类
 *
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public abstract class AbstractPagedGUI {

    protected final MagicBlockPlugin plugin;
    protected final GUIConfig guiConfig;

    // 玩家GUI状态管理
    protected final Map<UUID, GUIState> playerStates = new ConcurrentHashMap<>();

    /**
     * GUI状态数据
     */
    protected static class GUIState {
        int currentPage = 1;
        ItemStack originalItem;
        long lastOpenTime;
        List<Material> displayMaterials;

        public GUIState() {
            this.lastOpenTime = System.currentTimeMillis();
        }
    }

    /**
     * 分页信息
     */
    protected static class PageInfo {
        final int itemsPerPage;
        final int totalPages;
        final int startIndex;
        final int endIndex;

        PageInfo(int itemsPerPage, int totalPages, int startIndex, int endIndex) {
            this.itemsPerPage = itemsPerPage;
            this.totalPages = totalPages;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }
    }

    public AbstractPagedGUI(MagicBlockPlugin plugin) {
        this.plugin = plugin;
        this.guiConfig = new GUIConfig(plugin);
    }

    // ==================== 模板方法 - 子类必须实现 ====================

    /**
     * 检查玩家权限
     */
    protected abstract boolean checkPermission(Player player);

    /**
     * 获取要显示的材料列表
     */
    protected abstract List<Material> getDisplayMaterials(Player player);

    /**
     * 创建物品的lore
     */
    protected abstract List<String> createItemLore(Material material, Player player);

    /**
     * 处理物品点击
     */
    protected abstract void handleItemClick(Player player, ItemStack clickedItem, boolean isRightClick);

    /**
     * 获取GUI标题
     */
    protected abstract String getGUITitle();

    /**
     * 添加自定义按钮 (可选)
     */
    protected void addCustomButtons(Inventory gui, Player player) {
        // 默认不添加，子类可以覆盖
    }

    // ==================== 统一实现的公共方法 ====================

    /**
     * 打开GUI
     */
    public void openInventory(Player player) {
        if (!checkPermission(player)) {
            return;
        }

        // 初始化或获取玩家状态
        GUIState state = getOrCreateState(player);
        state.originalItem = player.getInventory().getItemInMainHand().clone();
        state.lastOpenTime = System.currentTimeMillis();
        state.currentPage = 1;
        state.displayMaterials = getDisplayMaterials(player);

        updateInventory(player);
    }

    /**
     * 更新GUI显示
     */
    public void updateInventory(Player player) {
        GUIState state = playerStates.get(player.getUniqueId());
        if (state == null) {
            return;
        }

        // 创建GUI框架
        Inventory gui = createGUIFrame(player, state);

        // 填充物品
        fillItems(gui, player, state);

        // 添加自定义按钮
        addCustomButtons(gui, player);

        // 打开GUI
        player.openInventory(gui);
    }

    /**
     * 创建GUI框架 (包含导航按钮)
     */
    private Inventory createGUIFrame(Player player, GUIState state) {
        String title = ChatColor.translateAlternateColorCodes('&', getGUITitle());
        Inventory gui = Bukkit.createInventory(null, guiConfig.getSize(), title);

        PageInfo pageInfo = calculatePagination(state.displayMaterials.size(), state.currentPage);

        // 添加导航按钮
        addNavigationButtons(gui, state.currentPage, pageInfo.totalPages);

        return gui;
    }

    /**
     * 添加导航按钮
     */
    private void addNavigationButtons(Inventory gui, int currentPage, int totalPages) {
        // 上一页按钮
        gui.setItem(guiConfig.getPreviousPageSlot(),
            guiConfig.createPreviousPageButton(currentPage > 1));

        // 下一页按钮
        gui.setItem(guiConfig.getNextPageSlot(),
            guiConfig.createNextPageButton(currentPage < totalPages));

        // 页码显示
        gui.setItem(guiConfig.getPageInfoSlot(),
            guiConfig.createPageInfoButton(currentPage, totalPages));
    }

    /**
     * 填充物品到GUI
     */
    private void fillItems(Inventory gui, Player player, GUIState state) {
        PageInfo pageInfo = calculatePagination(state.displayMaterials.size(), state.currentPage);

        int slot = 0;
        for (int i = pageInfo.startIndex; i < pageInfo.endIndex; i++) {
            Material material = state.displayMaterials.get(i);

            // 跳过导航按钮槽位
            while (isNavigationSlot(slot)) {
                slot++;
            }

            if (slot >= guiConfig.getSize()) {
                break;
            }

            ItemStack displayItem = createDisplayItem(material, player);
            gui.setItem(slot, displayItem);
            slot++;
        }
    }

    /**
     * 创建显示物品
     */
    protected ItemStack createDisplayItem(Material material, Player player) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // 设置显示名称
            String blockName = plugin.getMinecraftLangManager().getItemStackName(item);
            String nameFormat = plugin.getConfig().getString("display.block-name-format", "&b✦ %s &b✦");
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                String.format(nameFormat, blockName)));

            // 设置lore (委托给子类)
            List<String> lore = createItemLore(material, player);
            meta.setLore(lore);

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * 计算分页信息
     */
    protected PageInfo calculatePagination(int totalItems, int page) {
        int itemsPerPage = calculateItemsPerPage();
        int totalPages = Math.max(1, (int) Math.ceil(totalItems / (double) itemsPerPage));
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, totalItems);

        return new PageInfo(itemsPerPage, totalPages, startIndex, endIndex);
    }

    /**
     * 计算每页物品数量
     */
    protected int calculateItemsPerPage() {
        int totalSlots = guiConfig.getSize();
        // 保留槽位：上一页、下一页、页码显示
        int reservedSlots = 3;
        return totalSlots - reservedSlots;
    }

    /**
     * 处理GUI点击
     */
    public void handleClick(Player player, int slot, ItemStack clickedItem, boolean isRightClick) {
        GUIState state = playerStates.get(player.getUniqueId());
        if (state == null) {
            return;
        }

        synchronized (this) {
            // 处理导航按钮
            if (handleNavigationClick(player, slot, clickedItem, state)) {
                return;
            }

            // 委托给子类处理物品点击
            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                handleItemClick(player, clickedItem, isRightClick);
            }
        }
    }

    /**
     * 处理导航按钮点击
     *
     * @return true 表示是导航按钮，已处理
     */
    private boolean handleNavigationClick(Player player, int slot, ItemStack item, GUIState state) {
        PageInfo pageInfo = calculatePagination(state.displayMaterials.size(), state.currentPage);

        // 上一页
        if (slot == guiConfig.getPreviousPageSlot() &&
            guiConfig.matchesPreviousPageButton(item)) {
            if (state.currentPage > 1) {
                state.currentPage--;
                updateInventory(player);
            }
            return true;
        }

        // 下一页
        if (slot == guiConfig.getNextPageSlot() &&
            guiConfig.matchesNextPageButton(item)) {
            if (state.currentPage < pageInfo.totalPages) {
                state.currentPage++;
                updateInventory(player);
            }
            return true;
        }

        return false;
    }

    /**
     * 检查是否是导航槽位
     */
    private boolean isNavigationSlot(int slot) {
        return slot == guiConfig.getPreviousPageSlot() ||
               slot == guiConfig.getNextPageSlot() ||
               slot == guiConfig.getPageInfoSlot();
    }

    /**
     * 清理玩家数据
     */
    public void clearPlayerData(UUID playerUUID) {
        playerStates.remove(playerUUID);
    }

    /**
     * 获取或创建玩家状态
     */
    protected GUIState getOrCreateState(Player player) {
        return playerStates.computeIfAbsent(player.getUniqueId(), k -> new GUIState());
    }

    /**
     * 重新加载配置
     */
    public void reloadConfig() {
        guiConfig.loadConfig();
    }
}
