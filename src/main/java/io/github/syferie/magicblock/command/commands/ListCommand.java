package io.github.syferie.magicblock.command.commands;

import io.github.syferie.magicblock.MagicBlockPlugin;
import io.github.syferie.magicblock.command.ICommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * List 命令 - 打开方块选择GUI
 *
 * 用法: /mb list
 * 权限: magicblock.list
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public class ListCommand implements ICommand {

    private final MagicBlockPlugin plugin;

    public ListCommand(MagicBlockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, "commands.console-only-error");
            return;
        }

        Player player = (Player) sender;
        plugin.getListener().getGuiManager().getBlockSelectionGUI().openInventory(player);
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermissionNode());
    }

    @Override
    public String getPermissionNode() {
        return "magicblock.list";
    }

    @Override
    public String getUsage() {
        return "/mb list";
    }

    @Override
    public String getDescription() {
        return plugin.getMessage("commands.list-description");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
