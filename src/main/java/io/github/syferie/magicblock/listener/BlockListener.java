package io.github.syferie.magicblock.listener;

import io.github.syferie.magicblock.MagicBlockPlugin;
import io.github.syferie.magicblock.gui.GUIManager;
import io.github.syferie.magicblock.listener.handlers.*;
import org.bukkit.Material;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * 监听器协调器 - 管理所有专职事件监听器
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public class BlockListener implements Listener {

    private final MagicBlockPlugin plugin;
    private final GUIManager guiManager;
    private final List<Material> buildingMaterials;

    // 所有专职监听器
    private final List<Listener> handlers = new ArrayList<>();

    public BlockListener(MagicBlockPlugin plugin, List<Material> allowedMaterials) {
        this.plugin = plugin;
        this.buildingMaterials = new ArrayList<>(allowedMaterials);
        this.guiManager = new GUIManager(plugin, allowedMaterials);

        // 注册所有专职监听器
        registerHandlers();
    }

    /**
     * 注册所有专职监听器
     */
    private void registerHandlers() {
        // 创建所有专职监听器
        handlers.add(new BlockPlacementHandler(plugin));
        handlers.add(new BlockBreakHandler(plugin));
        handlers.add(new BlockProtectionHandler(plugin));
        handlers.add(new BlockExplosionHandler(plugin));
        handlers.add(new BlockInteractionHandler(plugin, guiManager));
        handlers.add(new BlockGUIHandler(plugin, guiManager));
        handlers.add(new BlockContainerHandler(plugin));
        handlers.add(new BlockCraftHandler(plugin));
        handlers.add(new BlockRedstoneHandler(plugin));
        handlers.add(new BlockEntityChangeHandler(plugin));

        // 将所有监听器注册到服务器
        for (Listener handler : handlers) {
            plugin.getServer().getPluginManager().registerEvents(handler, plugin);
        }

        plugin.debug("已注册 " + handlers.size() + " 个专职事件监听器");
    }

    // ==================== 遗留方法 - 保持兼容性 ====================

    /**
     * 获取GUI管理器
     */
    public GUIManager getGuiManager() {
        return guiManager;
    }

    /**
     * 设置允许的材料列表
     */
    public void setAllowedMaterials(List<Material> materials) {
        this.buildingMaterials.clear();
        this.buildingMaterials.addAll(materials);
    }

    /**
     * 重载GUI配置
     */
    public void reloadGUIConfig() {
        if (guiManager != null && guiManager.getBlockSelectionGUI() != null) {
            guiManager.getBlockSelectionGUI().reloadConfig();
        }
    }

    /**
     * 获取所有专职监听器
     *
     * @return 专职监听器列表
     */
    public List<Listener> getHandlers() {
        return new ArrayList<>(handlers);
    }
}
