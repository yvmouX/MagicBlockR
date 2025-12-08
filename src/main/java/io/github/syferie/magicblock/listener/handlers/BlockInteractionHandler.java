package io.github.syferie.magicblock.listener.handlers;

import io.github.syferie.magicblock.MagicBlockPlugin;
import io.github.syferie.magicblock.gui.GUIManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 方块交互监听器
 *
 * 职责：
 * - 处理Shift+左键打开GUI
 * - 处理斧头削皮/去氧化操作
 * - GUI冷却时间管理
 *
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public class BlockInteractionHandler extends BaseListener {

    private static final long GUI_OPEN_COOLDOWN = 300; // 毫秒
    private final Map<UUID, Long> lastGuiOpenTime = new HashMap<>();
    private final GUIManager guiManager;

    public BlockInteractionHandler(MagicBlockPlugin plugin, GUIManager guiManager) {
        super(plugin);
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();

        // 处理魔法方块交互 (斧头削皮/去氧化)
        if (clickedBlock != null && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            handleMagicBlockInteraction(event, player, clickedBlock);
        }

        // 处理Shift+左键打开GUI
        ItemStack item = event.getItem();
        if (item != null && blockManager.isMagicBlock(item)) {
            handleShiftLeftClick(event, player, item);
        }
    }

    /**
     * 处理魔法方块的特殊交互 (斧头削皮/去氧化)
     */
    private void handleMagicBlockInteraction(PlayerInteractEvent event, Player player, Block block) {
        if (!indexManager.isMagicBlock(block.getLocation())) {
            return;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // 检查是否是斧头操作
        if (itemInHand != null && isAxe(itemInHand.getType())) {
            handleAxeOperation(event, player, block, itemInHand);
        }
    }

    /**
     * 处理斧头削皮/去氧化操作
     */
    private void handleAxeOperation(PlayerInteractEvent event, Player player, Block block, ItemStack axe) {
        Material currentType = block.getType();
        Material newType = getStrippedOrScrapedType(currentType);

        if (newType == null || newType == currentType) {
            return; // 不是可削皮/去氧化的方块
        }

        event.setCancelled(true);

        // 检查权限
        if (!player.hasPermission("magicblock.use")) {
            plugin.sendMessage(player, "messages.no-permission-use");
            return;
        }

        // 检查绑定
        if (!checkBinding(player, new ItemStack(currentType))) {
            return;
        }

        // 改变方块类型
        block.setType(newType);

        // 播放音效
        playAxeSound(player, block, currentType);

        // 损耗斧头
        damageAxe(player, axe);
    }

    /**
     * 处理Shift+左键打开GUI
     */
    private void handleShiftLeftClick(PlayerInteractEvent event, Player player, ItemStack item) {
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        if (!player.isSneaking()) {
            return;
        }

        // 检查权限
        if (!player.hasPermission("magicblock.use")) {
            plugin.sendMessage(player, "messages.no-permission-use");
            event.setCancelled(true);
            return;
        }

        // 检查冷却时间
        if (!checkCooldown(player)) {
            return;
        }

        // 检查绑定
        if (!checkBinding(player, item)) {
            event.setCancelled(true);
            return;
        }

        // 打开GUI
        openGUI(player, event);
    }

    /**
     * 检查冷却时间
     */
    private boolean checkCooldown(Player player) {
        long currentTime = System.currentTimeMillis();
        Long lastTime = lastGuiOpenTime.get(player.getUniqueId());

        if (lastTime != null && currentTime - lastTime < GUI_OPEN_COOLDOWN) {
            return false;
        }

        lastGuiOpenTime.put(player.getUniqueId(), currentTime);
        return true;
    }

    /**
     * 检查绑定权限
     */
    private boolean checkBinding(Player player, ItemStack item) {
        // 使用配置缓存
        boolean bindingEnabled = plugin.getConfigCache().isBindingSystemEnabled();

        if (bindingEnabled) {
            UUID boundPlayer = plugin.getBlockBindManager().getBoundPlayer(item);
            if (boundPlayer != null && !boundPlayer.equals(player.getUniqueId())) {
                plugin.sendMessage(player, "messages.not-bound-to-you");
                return false;
            }
        }

        return true;
    }

    /**
     * 打开GUI
     */
    private void openGUI(Player player, PlayerInteractEvent event) {
        event.setCancelled(true);

        // 延迟打开GUI
        foliaLib.getScheduler().runLater(() -> {
            if (player.isOnline()) {
                foliaLib.getScheduler().runAtEntity(
                    player,
                    task -> guiManager.getBlockSelectionGUI().openInventory(player)
                );
            }
        }, 2L);
    }

    /**
     * 播放斧头音效
     */
    private void playAxeSound(Player player, Block block, Material originalType) {
        if (isLogType(originalType)) {
            player.getWorld().playSound(block.getLocation(), Sound.ITEM_AXE_STRIP, 1.0f, 1.0f);
        } else if (isCopperType(originalType)) {
            player.getWorld().playSound(block.getLocation(), Sound.ITEM_AXE_SCRAPE, 1.0f, 1.0f);
        }
    }

    /**
     * 损耗斧头耐久
     */
    private void damageAxe(Player player, ItemStack axe) {
        if (axe.getType().getMaxDurability() <= 0) {
            return;
        }

        org.bukkit.inventory.meta.Damageable damageable =
            (org.bukkit.inventory.meta.Damageable) axe.getItemMeta();

        if (damageable != null) {
            damageable.setDamage(damageable.getDamage() + 1);
            axe.setItemMeta(damageable);

            // 检查是否损坏
            if (damageable.getDamage() >= axe.getType().getMaxDurability()) {
                player.getInventory().setItemInMainHand(null);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            }
        }
    }

    // ==================== 辅助方法 - 使用 MaterialSets 优化 ====================

    private boolean isAxe(Material material) {
        return io.github.syferie.magicblock.util.MaterialSets.isAxe(material);
    }

    private boolean isLogType(Material material) {
        return io.github.syferie.magicblock.util.MaterialSets.isLogOrWood(material);
    }

    private boolean isCopperType(Material material) {
        return io.github.syferie.magicblock.util.MaterialSets.isCopperBlock(material);
    }

    private Material getStrippedOrScrapedType(Material material) {
        String name = material.name();

        // 削皮原木
        if (name.contains("LOG") && !name.contains("STRIPPED")) {
            try {
                return Material.valueOf("STRIPPED_" + name);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        // 削皮木头
        if (name.contains("WOOD") && !name.contains("STRIPPED")) {
            try {
                return Material.valueOf("STRIPPED_" + name);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        // 去氧化铜
        if (name.contains("COPPER")) {
            if (name.contains("WEATHERED")) {
                return Material.valueOf(name.replace("WEATHERED_", "EXPOSED_"));
            } else if (name.contains("EXPOSED")) {
                return Material.valueOf(name.replace("EXPOSED_", ""));
            } else if (name.contains("OXIDIZED")) {
                return Material.valueOf(name.replace("OXIDIZED_", "WEATHERED_"));
            }
        }

        return null;
    }
}
