package io.github.syferie.magicblock.gui;

import io.github.syferie.magicblock.MagicBlockPlugin;
import io.github.syferie.magicblock.manager.FavoriteManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 收藏GUI
 *
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public class FavoriteGUI extends AbstractPagedGUI {

    private final FavoriteManager favoriteManager;

    public FavoriteGUI(MagicBlockPlugin plugin, FavoriteManager favoriteManager) {
        super(plugin);
        this.favoriteManager = favoriteManager;
    }

    // ==================== 模板方法实现 ====================

    @Override
    protected boolean checkPermission(Player player) {
        if (!player.hasPermission("magicblock.use")) {
            plugin.sendMessage(player, "messages.no-permission-use");
            return false;
        }
        return true;
    }

    @Override
    protected List<Material> getDisplayMaterials(Player player) {
        return favoriteManager.getPlayerFavorites(player);
    }

    @Override
    protected List<String> createItemLore(Material material, Player player) {
        List<String> lore = new ArrayList<>();

        lore.add(ChatColor.translateAlternateColorCodes('&',
            plugin.getConfig().getString("gui.text.favorite-select", "&7» 左键选择此收藏方块")));
        lore.add(ChatColor.translateAlternateColorCodes('&',
            plugin.getConfig().getString("gui.text.favorite-remove", "&c» 右键点击取消收藏")));

        return lore;
    }

    @Override
    protected void handleItemClick(Player player, ItemStack clickedItem, boolean isRightClick) {
        Material material = clickedItem.getType();

        if (isRightClick) {
            // 取消收藏
            favoriteManager.toggleFavorite(player, material);
            updateInventory(player);
        } else {
            // 选择收藏的方块
            selectFavoriteBlock(player, material);
        }
    }

    @Override
    protected String getGUITitle() {
        return plugin.getConfig().getString("gui.text.favorites-title", "&8⚡ &b我的收藏");
    }

    // ==================== 收藏特有逻辑 ====================

    /**
     * 选择收藏的方块
     */
    private void selectFavoriteBlock(Player player, Material material) {
        // 获取原始物品
        GUIState state = playerStates.get(player.getUniqueId());
        if (state == null || state.originalItem == null) {
            player.closeInventory();
            return;
        }

        ItemStack originalItem = state.originalItem;

        // 检查原始物品是否是魔法方块
        if (!plugin.getBlockManager().isMagicBlock(originalItem)) {
            plugin.sendMessage(player, "messages.not-magic-block");
            player.closeInventory();
            return;
        }

        // 获取原始物品的使用次数
        int useTimes = plugin.getBlockManager().getUseTimes(originalItem);

        // 创建新的魔法方块
        ItemStack newBlock = new ItemStack(material);
        plugin.getBlockManager().setUseTimes(newBlock, useTimes);

        // 复制绑定信息
        if (plugin.getBlockBindManager().isBlockBound(originalItem)) {
            plugin.getBlockBindManager().updateBlockMaterial(newBlock);
        }

        // 替换手持物品
        player.getInventory().setItemInMainHand(newBlock);
        player.closeInventory();

        plugin.sendMessage(player, "messages.block-changed", material.name());
    }
}
