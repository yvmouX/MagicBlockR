package io.github.syferie.magicblock.util;

import io.github.syferie.magicblock.MagicBlockPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Lore工具类 - 统一管理魔法方块和魔法食物的lore生成逻辑
 *
 */
public final class LoreUtil {

    private LoreUtil() {
        // 工具类不允许实例化
    }

    /**
     * 生成进度条字符串
     *
     * @param remainingTimes 剩余次数
     * @param maxTimes 最大次数
     * @param barLength 进度条长度
     * @param filledChar 已填充字符
     * @param emptyChar 未填充字符
     * @return 格式化的进度条字符串
     */
    public static String generateProgressBar(int remainingTimes, int maxTimes, int barLength,
                                            String filledChar, String emptyChar) {
        double usedPercentage = (double) remainingTimes / maxTimes;
        int filledBars = (int) Math.round(usedPercentage * barLength);

        StringBuilder progressBar = new StringBuilder();
        progressBar.append(ChatColor.GRAY).append("[");

        for (int i = 0; i < barLength; i++) {
            if (i < filledBars) {
                progressBar.append(ChatColor.GREEN).append(filledChar);
            } else {
                progressBar.append(ChatColor.GRAY).append(emptyChar);
            }
        }

        progressBar.append(ChatColor.GRAY).append("]");
        return progressBar.toString();
    }

    /**
     * 生成进度条字符串（使用默认字符）
     *
     * @param remainingTimes 剩余次数
     * @param maxTimes 最大次数
     * @param barLength 进度条长度
     * @return 格式化的进度条字符串
     */
    public static String generateProgressBar(int remainingTimes, int maxTimes, int barLength) {
        return generateProgressBar(remainingTimes, maxTimes, barLength, "■", "■");
    }

    /**
     * 生成使用次数显示文本
     *
     * @param prefix 前缀文本（如 "Uses:" 或 "剩余使用次数:"）
     * @param remainingTimes 剩余次数
     * @param maxTimes 最大次数
     * @param isInfinite 是否是无限次数
     * @param infiniteSymbol 无限符号（如 "∞"）
     * @return 格式化的使用次数字符串
     */
    public static String generateUsageText(String prefix, int remainingTimes, int maxTimes,
                                          boolean isInfinite, String infiniteSymbol) {
        StringBuilder usageText = new StringBuilder();
        usageText.append(ChatColor.GRAY).append(prefix).append(" ");

        if (isInfinite) {
            usageText.append(ChatColor.AQUA).append(infiniteSymbol)
                    .append(ChatColor.GRAY).append("/")
                    .append(ChatColor.GRAY).append(infiniteSymbol);
        } else {
            usageText.append(ChatColor.AQUA).append(remainingTimes)
                    .append(ChatColor.GRAY).append("/")
                    .append(ChatColor.GRAY).append(maxTimes);
        }

        return usageText.toString();
    }

    /**
     * 生成使用次数显示文本（使用默认无限符号）
     *
     * @param prefix 前缀文本
     * @param remainingTimes 剩余次数
     * @param maxTimes 最大次数
     * @param isInfinite 是否是无限次数
     * @return 格式化的使用次数字符串
     */
    public static String generateUsageText(String prefix, int remainingTimes, int maxTimes, boolean isInfinite) {
        return generateUsageText(prefix, remainingTimes, maxTimes, isInfinite, "∞");
    }

    /**
     * 检查是否是无限次数
     *
     * @param maxTimes 最大次数
     * @return 如果是无限次数返回true
     */
    public static boolean isInfiniteUses(int maxTimes) {
        return maxTimes == Integer.MAX_VALUE - 100;
    }

    /**
     * 处理装饰性lore行，支持PlaceholderAPI变量替换
     *
     * @param line 原始lore行
     * @param player 玩家（可以为null）
     * @return 处理后的lore行
     */
    public static String processDecorativeLore(String line, Player player) {
        String processedLine = ChatColor.translateAlternateColorCodes('&', line);

        // 如果服务器安装了PlaceholderAPI，处理变量
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            processedLine = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, processedLine);
        }

        return processedLine;
    }

    /**
     * 批量处理装饰性lore列表
     *
     * @param lines 原始lore行列表
     * @param player 玩家（可以为null）
     * @return 处理后的lore列表
     */
    public static List<String> processDecorativeLoreList(List<String> lines, Player player) {
        List<String> processedLines = new ArrayList<>();
        for (String line : lines) {
            processedLines.add(processDecorativeLore(line, player));
        }
        return processedLines;
    }

    /**
     * 生成绑定玩家的lore行
     *
     * @param prefix 前缀（如 "绑定玩家: "）
     * @param playerUUID 玩家UUID
     * @return 格式化的绑定信息字符串
     */
    public static String generateBindingLore(String prefix, UUID playerUUID) {
        if (playerUUID == null) {
            return null;
        }

        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            return prefix + player.getName();
        } else {
            return prefix + playerUUID.toString();
        }
    }
}
