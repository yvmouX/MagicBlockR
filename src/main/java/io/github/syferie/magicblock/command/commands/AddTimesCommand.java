package io.github.syferie.magicblock.command.commands;

import io.github.syferie.magicblock.MagicBlockPlugin;
import io.github.syferie.magicblock.command.ICommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

/**
 * AddTimes 命令 - 增加手持魔法方块的使用次数
 *
 * 用法: /mb addtimes <times>
 * 权限: magicblock.addtimes
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public class AddTimesCommand implements ICommand {

    private final MagicBlockPlugin plugin;

    public AddTimesCommand(MagicBlockPlugin plugin) {
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
        ItemStack item = player.getInventory().getItemInMainHand();

        // 检查是否是魔法方块
        if (!plugin.getBlockManager().isMagicBlock(item)) {
            plugin.sendMessage(player, "commands.not-magic-block");
            return;
        }

        // 解析要增加的次数
        int addTimes;
        try {
            addTimes = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            plugin.sendMessage(player, "commands.invalid-number", args[0]);
            return;
        }

        // 获取当前使用次数并增加
        int currentTimes = plugin.getBlockManager().getUseTimes(item);
        int newTimes = currentTimes + addTimes;

        plugin.getBlockManager().setUseTimes(item, newTimes);
        plugin.sendMessage(player, "commands.addtimes.success", addTimes, newTimes);
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermissionNode());
    }

    @Override
    public String getPermissionNode() {
        return "magicblock.addtimes";
    }

    @Override
    public String getUsage() {
        return "/mb addtimes <times>";
    }

    @Override
    public String getDescription() {
        return plugin.getMessage("commands.addtimes-description");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("10", "50", "100", "500", "1000");
        }
        return Collections.emptyList();
    }
}
