package io.github.syferie.magicblock.command.commands;

import io.github.syferie.magicblock.MagicBlockPlugin;
import io.github.syferie.magicblock.command.ICommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Give 命令 - 给予玩家魔法方块
 *
 * 用法: /mb give <player> <material> [amount] [uses]
 * 权限: magicblock.give
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public class GiveCommand implements ICommand {

    private final MagicBlockPlugin plugin;

    public GiveCommand(MagicBlockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(getUsage());
            return;
        }

        // 解析参数
        String playerName = args[0];
        String materialName = args[1];
        int amount = args.length >= 3 ? parseIntSafe(args[2], 1) : 1;
        int useTimes = args.length >= 4 ? parseIntSafe(args[3], plugin.getDefaultBlockTimes()) : plugin.getDefaultBlockTimes();

        // 查找玩家
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            plugin.sendMessage(sender, "commands.player-not-found", playerName);
            return;
        }

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
            target.getInventory().addItem(item.clone());
        }

        // 发送消息
        plugin.sendMessage(sender, "commands.give.success",
            amount, material.name(), target.getName(), useTimes);
        plugin.sendMessage(target, "commands.give.received",
            amount, material.name(), useTimes);
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermissionNode());
    }

    @Override
    public String getPermissionNode() {
        return "magicblock.give";
    }

    @Override
    public String getUsage() {
        return "/mb give <player> <material> [amount] [uses]";
    }

    @Override
    public String getDescription() {
        return plugin.getMessage("commands.give-description");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            // 玩家名补全
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        } else if (args.length == 2) {
            // 材料补全
            return Arrays.stream(Material.values())
                .filter(Material::isBlock)
                .map(Material::name)
                .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                .limit(20)
                .collect(Collectors.toList());
        } else if (args.length == 3) {
            // 数量建议
            return Arrays.asList("1", "8", "16", "32", "64");
        } else if (args.length == 4) {
            // 使用次数建议
            return Arrays.asList("10", "50", "100", "500", "-1");
        }
        return new ArrayList<>();
    }

    private int parseIntSafe(String str, int defaultValue) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
