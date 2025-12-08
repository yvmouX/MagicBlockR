package io.github.syferie.magicblock.listener.handlers;

import io.github.syferie.magicblock.MagicBlockPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * 容器交互监听器
 *
 * 职责：
 * - 防止魔法方块被放入功能性容器 (熔炉、酿造台等)
 * - 处理绑定方块GUI的交互
 * - 处理方块找回和删除
 *
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public class BlockContainerHandler extends BaseListener {

    public BlockContainerHandler(MagicBlockPlugin plugin) {
        super(plugin);
    }

    /**
     * 处理容器点击事件
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        // 1. 检查功能性容器限制
        if (isFunctionalContainer(event.getInventory().getType())) {
            if (preventMagicBlockInContainer(event, player)) {
                return; // 已处理并取消
            }
        }

        // 2. 处理绑定方块GUI交互
        handleBindListGUI(event, player);
    }

    /**
     * 处理容器拖拽事件
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        // 检查是否是功能性容器
        if (!isFunctionalContainer(event.getInventory().getType())) {
            return;
        }

        ItemStack draggedItem = event.getOldCursor();

        // 检查拖拽的物品是否是魔法方块
        if (draggedItem != null && blockManager.isMagicBlock(draggedItem)) {
            // 检查拖拽的目标槽位是否在功能性容器中
            for (int slot : event.getRawSlots()) {
                if (slot < event.getInventory().getSize()) {
                    event.setCancelled(true);
                    plugin.sendMessage(player, "messages.cannot-place-in-functional-container");
                    return;
                }
            }
        }
    }

    /**
     * 防止魔法方块被放入功能性容器
     *
     * @return true 表示已处理并取消事件
     */
    private boolean preventMagicBlockInContainer(InventoryClickEvent event, Player player) {
        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        // 检查当前点击的物品
        if (currentItem != null && blockManager.isMagicBlock(currentItem)) {
            event.setCancelled(true);
            plugin.sendMessage(player, "messages.cannot-place-in-functional-container");
            return true;
        }

        // 检查鼠标上的物品
        if (cursorItem != null && blockManager.isMagicBlock(cursorItem)) {
            event.setCancelled(true);
            plugin.sendMessage(player, "messages.cannot-place-in-functional-container");
            return true;
        }

        // 检查Shift+点击操作
        if (event.isShiftClick() && event.getCurrentItem() != null) {
            ItemStack clickedItem = event.getCurrentItem();
            if (blockManager.isMagicBlock(clickedItem)) {
                event.setCancelled(true);
                plugin.sendMessage(player, "messages.cannot-place-in-functional-container");
                return true;
            }
        }

        return false;
    }

    /**
     * 处理绑定方块GUI交互
     */
    private void handleBindListGUI(InventoryClickEvent event, Player player) {
        String title = ChatColor.stripColor(event.getView().getTitle());
        String boundBlocksTitle = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
            plugin.getConfig().getString("gui.text.bound-blocks-title", "&8⚡ &b已绑定方块")));

        // 只处理绑定方块GUI
        if (!title.equals(boundBlocksTitle)) {
            return;
        }

        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        // 右键：双击删除或找回
        if (event.isRightClick()) {
            plugin.getBlockBindManager().handleBindListClick(player, clickedItem);
        } else {
            // 左键：找回方块
            retrieveBlockFromList(player, clickedItem);
        }
    }

    /**
     * 从绑定列表找回方块
     */
    private void retrieveBlockFromList(Player player, ItemStack clickedItem) {
        ItemMeta clickedMeta = clickedItem.getItemMeta();
        if (clickedMeta == null) return;

        NamespacedKey blockIdKey = new NamespacedKey(plugin, "block_id");
        String blockId = clickedMeta.getPersistentDataContainer()
            .get(blockIdKey, PersistentDataType.STRING);

        if (blockId == null) return;

        // 检查玩家背包中是否已有相同ID的方块
        if (playerAlreadyHasBlock(player, blockId)) {
            plugin.sendMessage(player, "messages.already-have-block");
            return;
        }

        // 找回方块
        plugin.getBlockBindManager().retrieveBlock(player, clickedItem);
        player.closeInventory();
    }

    /**
     * 检查玩家是否已拥有相同ID的方块
     */
    private boolean playerAlreadyHasBlock(Player player, String blockId) {
        NamespacedKey blockIdKey = new NamespacedKey(plugin, "block_id");

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && blockManager.isMagicBlock(item)) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    String existingBlockId = meta.getPersistentDataContainer()
                        .get(blockIdKey, PersistentDataType.STRING);
                    if (blockId.equals(existingBlockId)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 检查是否是功能性容器
     */
    private boolean isFunctionalContainer(InventoryType type) {
        return type == InventoryType.FURNACE ||
               type == InventoryType.BLAST_FURNACE ||
               type == InventoryType.SMOKER ||
               type == InventoryType.BREWING ||
               type == InventoryType.ANVIL ||
               type == InventoryType.GRINDSTONE ||
               type == InventoryType.STONECUTTER ||
               type == InventoryType.SMITHING ||
               type == InventoryType.ENCHANTING ||
               type == InventoryType.BEACON ||
               type == InventoryType.HOPPER ||
               type == InventoryType.DISPENSER ||
               type == InventoryType.DROPPER;
    }
}
