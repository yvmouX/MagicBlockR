package io.github.syferie.magicblock.command;

import io.github.syferie.magicblock.MagicBlockPlugin;
import io.github.syferie.magicblock.command.commands.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

/**
 * 命令管理器 - 使用命令模式重构
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public class CommandManager implements CommandExecutor {

    private final MagicBlockPlugin plugin;
    private final Map<String, ICommand> commands = new HashMap<>();

    public CommandManager(MagicBlockPlugin plugin) {
        this.plugin = plugin;
        registerCommands();
    }

    /**
     * 注册所有命令
     */
    private void registerCommands() {
        // 注册各个命令
        register("help", new HelpCommand(plugin));
        register("list", new ListCommand(plugin));
        register("get", new GetCommand(plugin));
        register("give", new GiveCommand(plugin));
        register("getfood", new GetFoodCommand(plugin));
        register("settimes", new SetTimesCommand(plugin));
        register("addtimes", new AddTimesCommand(plugin));
        register("reload", new ReloadCommand(plugin));
    }

    /**
     * 注册单个命令
     */
    private void register(String name, ICommand command) {
        commands.put(name.toLowerCase(), command);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 如果没有参数，显示帮助
        if (args.length == 0) {
            ICommand helpCmd = commands.get("help");
            if (helpCmd != null) {
                helpCmd.execute(sender, new String[0]);
            }
            return true;
        }

        // 获取子命令
        String subCommand = args[0].toLowerCase();
        ICommand cmd = commands.get(subCommand);

        if (cmd == null) {
            // 未知命令，显示帮助
            plugin.sendMessage(sender, "commands.unknown-command", subCommand);
            ICommand helpCmd = commands.get("help");
            if (helpCmd != null) {
                helpCmd.execute(sender, new String[0]);
            }
            return true;
        }

        // 检查权限
        if (!cmd.hasPermission(sender)) {
            plugin.sendMessage(sender, "messages.no-permission");
            return true;
        }

        // 执行命令 (去掉第一个参数，即子命令名)
        String[] cmdArgs = new String[args.length - 1];
        System.arraycopy(args, 1, cmdArgs, 0, cmdArgs.length);

        try {
            cmd.execute(sender, cmdArgs);
        } catch (Exception e) {
            plugin.getLogger().severe("执行命令时发生错误: " + subCommand);
            e.printStackTrace();
            plugin.sendMessage(sender, "commands.execution-error");
        }

        return true;
    }

    /**
     * 获取所有已注册的命令
     */
    public Map<String, ICommand> getCommands() {
        return new HashMap<>(commands);
    }
}
