package io.github.syferie.magicblock.command.handler;

import io.github.syferie.magicblock.MagicBlockPlugin;
import io.github.syferie.magicblock.command.CommandManager;
import io.github.syferie.magicblock.command.ICommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tab补全处理器 - 重构为配合命令模式
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public class TabCompleter implements org.bukkit.command.TabCompleter {

    private final MagicBlockPlugin plugin;
    private final CommandManager commandManager;

    public TabCompleter(MagicBlockPlugin plugin) {
        this.plugin = plugin;
        this.commandManager = (CommandManager) plugin.getCommand("magicblock").getExecutor();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("magicblock")) {
            return new ArrayList<>();
        }

        // 第一个参数：子命令补全
        if (args.length == 1) {
            return completeSubCommand(sender, args[0]);
        }

        // 后续参数：委托给具体命令的 tabComplete 方法
        if (args.length >= 2) {
            return delegateToCommand(sender, args);
        }

        return new ArrayList<>();
    }

    /**
     * 补全子命令
     */
    private List<String> completeSubCommand(CommandSender sender, String input) {
        return commandManager.getCommands().entrySet().stream()
            .filter(entry -> entry.getValue().hasPermission(sender))
            .map(entry -> entry.getKey())
            .filter(name -> name.startsWith(input.toLowerCase()))
            .collect(Collectors.toList());
    }

    /**
     * 委托给具体命令进行补全
     */
    private List<String> delegateToCommand(CommandSender sender, String[] args) {
        String subCommand = args[0].toLowerCase();
        ICommand cmd = commandManager.getCommands().get(subCommand);

        if (cmd == null || !cmd.hasPermission(sender)) {
            return new ArrayList<>();
        }

        // 去掉第一个参数（子命令名），传递给命令的 tabComplete 方法
        String[] cmdArgs = new String[args.length - 1];
        System.arraycopy(args, 1, cmdArgs, 0, cmdArgs.length);

        return cmd.tabComplete(sender, cmdArgs);
    }
}
