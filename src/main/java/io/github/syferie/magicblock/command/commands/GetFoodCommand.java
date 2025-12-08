package io.github.syferie.magicblock.command.commands;

import io.github.syferie.magicblock.MagicBlockPlugin;
import io.github.syferie.magicblock.command.ICommand;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GetFood 命令 - 获取魔法食物
 *
 * 用法: /mb getfood <material> [amount] [uses]
 * 权限: magicblock.getfood
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public class GetFoodCommand implements ICommand {

    private final MagicBlockPlugin plugin;

    public GetFoodCommand(MagicBlockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, "commands.console-only-error");
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(getUsage());
            return;
        }

        Player player = (Player) sender;

        // 解析参数
        String materialName = args[0];
        int amount = args.length >= 2 ? parseIntSafe(args[1], 1) : 1;
        int useTimes = args.length >= 3 ? parseIntSafe(args[2], -1) : -1;

        // 解析材料
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.sendMessage(sender, "commands.invalid-material", materialName);
            return;
        }

        // 检查是否是配置的魔法食物
        if (!plugin.getFoodConfig().contains("foods." + material.name())) {
            plugin.sendMessage(sender, "commands.not-magic-food", materialName);
            return;
        }

        // 创建魔法食物
        ItemStack item = plugin.getMagicFood().createMagicFood(material);
        if (item == null) {
            plugin.sendMessage(sender, "commands.food-creation-failed", materialName);
            return;
        }

        // 如果指定了使用次数，设置它
        if (useTimes != -1) {
            plugin.getMagicFood().setUseTimes(item, useTimes);
        }

        // 给予物品
        for (int i = 0; i < amount; i++) {
            player.getInventory().addItem(item.clone());
        }

        plugin.sendMessage(player, "commands.getfood.success", amount, material.name());
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermissionNode());
    }

    @Override
    public String getPermissionNode() {
        return "magicblock.getfood";
    }

    @Override
    public String getUsage() {
        return "/mb getfood <material> [amount] [uses]";
    }

    @Override
    public String getDescription() {
        return plugin.getMessage("commands.getfood-description");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            // 从配置中获取魔法食物材料
            return plugin.getFoodConfig().getConfigurationSection("foods").getKeys(false).stream()
                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        } else if (args.length == 2) {
            return Arrays.asList("1", "8", "16", "32", "64");
        } else if (args.length == 3) {
            return Arrays.asList("10", "50", "100", "-1");
        }
        return Collections.emptyList();
    }

    private int parseIntSafe(String str, int defaultValue) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
