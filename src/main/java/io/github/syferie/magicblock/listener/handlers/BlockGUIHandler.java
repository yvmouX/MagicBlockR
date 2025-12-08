package io.github.syferie.magicblock.listener.handlers;

import io.github.syferie.magicblock.MagicBlockPlugin;
import io.github.syferie.magicblock.gui.GUIManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * GUI交互监听器
 *
 * 职责：
 * - 处理GUI搜索功能的聊天监听
 * - 处理物品槽切换时的GUI状态管理
 *
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public class BlockGUIHandler extends BaseListener {

    private final GUIManager guiManager;

    public BlockGUIHandler(MagicBlockPlugin plugin, GUIManager guiManager) {
        super(plugin);
        this.guiManager = guiManager;
    }

    /**
     * 处理玩家聊天 - GUI搜索功能
     */
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!GUIManager.isPlayerSearching(player)) {
            return;
        }

        event.setCancelled(true);
        String input = event.getMessage();

        // 处理取消搜索
        if (input.equalsIgnoreCase("cancel")) {
            GUIManager.setPlayerSearching(player, false);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                guiManager.getBlockSelectionGUI().openInventory(player);
            });
            return;
        }

        // 处理搜索
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            guiManager.getBlockSelectionGUI().handleSearch(player, input);
            GUIManager.setPlayerSearching(player, false);
        });
    }

    /**
     * 处理物品槽切换 - 退出搜索模式
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        if (!GUIManager.isPlayerSearching(player)) {
            return;
        }

        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        ItemMeta meta = (newItem != null) ? newItem.getItemMeta() : null;
        boolean hasSpecialLore = plugin.hasMagicLore(meta);

        // 如果切换到非魔法方块，退出搜索模式
        if (!hasSpecialLore) {
            GUIManager.setPlayerSearching(player, false);
            player.sendMessage(plugin.getMessage("messages.item-changed"));
        }
    }
}
