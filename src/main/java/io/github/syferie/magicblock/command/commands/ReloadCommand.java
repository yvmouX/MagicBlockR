package io.github.syferie.magicblock.command.commands;

import io.github.syferie.magicblock.MagicBlockPlugin;
import io.github.syferie.magicblock.command.ICommand;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

/**
 * Reload 命令 - 重载插件配置
 *
 * 用法: /mb reload
 * 权限: magicblock.reload
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public class ReloadCommand implements ICommand {

    private final MagicBlockPlugin plugin;

    public ReloadCommand(MagicBlockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // 重载配置
        plugin.reloadPluginAllowedMaterials();

        // 发送成功消息
        plugin.sendMessage(sender, "messages.reload-success");
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermissionNode());
    }

    @Override
    public String getPermissionNode() {
        return "magicblock.reload";
    }

    @Override
    public String getUsage() {
        return "/mb reload";
    }

    @Override
    public String getDescription() {
        return plugin.getMessage("commands.reload-description");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList(); // reload 命令没有参数
    }
}
