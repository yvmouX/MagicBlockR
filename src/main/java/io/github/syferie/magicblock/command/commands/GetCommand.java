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
 * Get 命令 - 获取魔法方块
 *
 * 用法: /mb get <material> [amount] [uses]
 * 权限: magicblock.get
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public class GetCommand implements ICommand {

    private final MagicBlockPlugin plugin;

    public GetCommand(MagicBlockPlugin plugin) {
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
        int useTimes = args.length >= 3 ? parseIntSafe(args[2], plugin.getDefaultBlockTimes()) : plugin.getDefaultBlockTimes();

        // 解析材料
        Material material = Material.getMaterial(materialName.toUpperCase());
        if (material == null || !material.isBlock()) {
            plugin.sendMessage(sender, "commands.invalid-material", materialName);
            return;
        }

        // 创建魔法方块
        ItemStack item = new ItemStack(material);
        plugin.getBlockManager().setUseTimes(item, useTimes);

        // 给予物品
        for (int i = 0; i < amount; i++) {
            player.getInventory().addItem(item.clone());
        }

        plugin.sendMessage(player, "commands.get.success", amount, material.name(), useTimes);
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermissionNode());
    }

    @Override
    public String getPermissionNode() {
        return "magicblock.get";
    }

    @Override
    public String getUsage() {
        return "/mb get <material> [amount] [uses]";
    }

    @Override
    public String getDescription() {
        return plugin.getMessage("commands.get-description");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            // 材料补全
            return Arrays.stream(Material.values())
                .filter(Material::isBlock)
                .map(Material::name)
                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                .limit(20)
                .collect(Collectors.toList());
        } else if (args.length == 2) {
            return Arrays.asList("1", "8", "16", "32", "64");
        } else if (args.length == 3) {
            return Arrays.asList("10", "50", "100", "500", "-1");
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
