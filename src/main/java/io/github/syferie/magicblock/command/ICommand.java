package io.github.syferie.magicblock.command;

import org.bukkit.command.CommandSender;

/**
 * 命令接口 - 使用命令模式重构
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public interface ICommand {

    /**
     * 执行命令
     *
     * @param sender 命令发送者
     * @param args 命令参数 (不包含子命令名)
     */
    void execute(CommandSender sender, String[] args);

    /**
     * 检查发送者是否有权限执行此命令
     *
     * @param sender 命令发送者
     * @return 如果有权限返回true
     */
    boolean hasPermission(CommandSender sender);

    /**
     * 获取命令所需的权限节点
     *
     * @return 权限节点 (如 "magicblock.give")
     */
    String getPermissionNode();

    /**
     * 获取命令用法说明
     *
     * @return 用法字符串 (如 "/mb give <player> <material> [amount] [uses]")
     */
    String getUsage();

    /**
     * 获取命令描述
     *
     * @return 命令描述
     */
    String getDescription();

    /**
     * Tab补全
     *
     * @param sender 命令发送者
     * @param args 当前参数
     * @return 补全建议列表
     */
    default java.util.List<String> tabComplete(CommandSender sender, String[] args) {
        return java.util.Collections.emptyList();
    }
}
