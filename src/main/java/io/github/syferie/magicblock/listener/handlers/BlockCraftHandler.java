package io.github.syferie.magicblock.listener.handlers;

import io.github.syferie.magicblock.MagicBlockPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;

/**
 * 方块合成/熔炼监听器
 *
 * 职责：
 * - 防止魔法方块被合成
 * - 防止魔法方块被熔炼
 * - 防止利用漏洞复制魔法方块
 *
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public class BlockCraftHandler extends BaseListener {

    public BlockCraftHandler(MagicBlockPlugin plugin) {
        super(plugin);
    }

    /**
     * 防止魔法方块被合成
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        // 检查合成配方中是否包含魔法方块
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (item != null && blockManager.isMagicBlock(item)) {
                event.setCancelled(true);
                plugin.sendMessage((Player) event.getWhoClicked(),
                    "messages.cannot-craft-with-magic-block");
                return;
            }
        }
    }

    /**
     * 防止魔法方块被熔炼
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        ItemStack source = event.getSource();

        // 如果熔炼源是魔法方块，取消熔炼
        if (blockManager.isMagicBlock(source)) {
            event.setCancelled(true);
        }
    }
}
