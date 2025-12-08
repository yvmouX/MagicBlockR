package io.github.syferie.magicblock.command.commands;

import io.github.syferie.magicblock.MagicBlockPlugin;
import io.github.syferie.magicblock.command.ICommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Help 命令 - 显示帮助信息
 *
 * 用法: /mb help
 * 权限: magicblock.help
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public class HelpCommand implements ICommand {

    private final MagicBlockPlugin plugin;

    public HelpCommand(MagicBlockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, "commands.console-only-error");
            return;
        }

        Player player = (Player) sender;
        sendHelpMessage(player);
    }

    /**
     * 发送帮助消息
     */
    private void sendHelpMessage(Player player) {
        List<String> helpMessages = new ArrayList<>();

        helpMessages.add(ChatColor.GOLD + "========= MagicBlock 帮助 =========");
        helpMessages.add(ChatColor.YELLOW + "/mb list" + ChatColor.GRAY + " - 打开方块选择界面");
        helpMessages.add(ChatColor.YELLOW + "/mb get <material> [amount] [uses]" + ChatColor.GRAY + " - 获取魔法方块");
        helpMessages.add(ChatColor.YELLOW + "/mb give <player> <material> [amount] [uses]" + ChatColor.GRAY + " - 给予玩家魔法方块");
        helpMessages.add(ChatColor.YELLOW + "/mb getfood <material> [amount]" + ChatColor.GRAY + " - 获取魔法食物");
        helpMessages.add(ChatColor.YELLOW + "/mb settimes <times>" + ChatColor.GRAY + " - 设置手持方块使用次数");
        helpMessages.add(ChatColor.YELLOW + "/mb addtimes <times>" + ChatColor.GRAY + " - 增加手持方块使用次数");
        helpMessages.add(ChatColor.YELLOW + "/mb reload" + ChatColor.GRAY + " - 重载配置");
        helpMessages.add(ChatColor.GOLD + "================================");

        for (String message : helpMessages) {
            player.sendMessage(message);
        }
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermissionNode());
    }

    @Override
    public String getPermissionNode() {
        return "magicblock.help";
    }

    @Override
    public String getUsage() {
        return "/mb help";
    }

    @Override
    public String getDescription() {
        return plugin.getMessage("commands.help-description");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
