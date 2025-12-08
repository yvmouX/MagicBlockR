package io.github.syferie.magicblock.hook;

import io.github.syferie.magicblock.MagicBlockPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PlaceholderHook extends PlaceholderExpansion {

    private final MagicBlockPlugin plugin;

    public PlaceholderHook(MagicBlockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "magicblock";
    }

    @Override
    public @NotNull String getAuthor() {
        return "WeSif";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        // 获取玩家使用魔法方块的总次数
        if (params.equalsIgnoreCase("block_uses")) {
            return String.valueOf(plugin.getPlayerUsage(player.getUniqueId()));
        }

        // 魔法食物 - 手持物品的剩余使用次数
        if (params.equalsIgnoreCase("magicfood_remaining_uses")) {
            try {
                if (player.isOnline() && player.getPlayer() != null && plugin.getMagicFood() != null) {
                    ItemStack item = player.getPlayer().getInventory().getItemInMainHand();
                    if (item != null && plugin.getMagicFood().isMagicFood(item)) {
                        int remainingUses = plugin.getMagicFood().getUseTimes(item);
                        return String.valueOf(Math.max(0, remainingUses)); // 确保不返回负数
                    }
                }
            } catch (Exception e) {
                // 记录错误但不抛出异常，返回默认值
                plugin.getLogger().warning("Error getting magicfood_remaining_uses placeholder: " + e.getMessage());
            }
            return "0";
        }

        if (params.equalsIgnoreCase("magicfood_max_uses")) {
            try {
                if (player.isOnline() && player.getPlayer() != null && plugin.getMagicFood() != null) {
                    ItemStack item = player.getPlayer().getInventory().getItemInMainHand();
                    if (item != null && plugin.getMagicFood().isMagicFood(item)) {
                        int maxUses = plugin.getMagicFood().getMaxUseTimes(item);
                        return String.valueOf(Math.max(0, maxUses)); // 确保不返回负数
                    }
                }
            } catch (Exception e) {
                // 记录错误但不抛出异常，返回默认值
                plugin.getLogger().warning("Error getting magicfood_max_uses placeholder: " + e.getMessage());
            }
            return "0";
        }

        if (params.equalsIgnoreCase("magicfood_uses_progress")) {
            try {
                if (player.isOnline() && player.getPlayer() != null && plugin.getMagicFood() != null) {
                    ItemStack item = player.getPlayer().getInventory().getItemInMainHand();
                    if (item != null && plugin.getMagicFood().isMagicFood(item)) {
                        int maxUses = plugin.getMagicFood().getMaxUseTimes(item);
                        int remainingUses = plugin.getMagicFood().getUseTimes(item);
                        
                        // 检查是否是无限次数
                        if (maxUses == Integer.MAX_VALUE - 100) {
                            return "100.0"; // 无限次数显示为100%
                        }
                        
                        // 防止除零错误
                        if (maxUses > 0) {
                            // 确保计算结果在合理范围内
                            int usedUses = Math.max(0, maxUses - Math.max(0, remainingUses));
                            double progress = ((double) usedUses / maxUses) * 100;
                            progress = Math.max(0.0, Math.min(100.0, progress)); // 限制在0-100范围内
                            return String.format("%.1f", progress);
                        }
                    }
                }
            } catch (Exception e) {
                // 记录错误但不抛出异常，返回默认值
                plugin.getLogger().warning("Error getting magicfood_uses_progress placeholder: " + e.getMessage());
            }
            return "0.0";
        }

        // 获取玩家剩余的魔法方块使用次数
        if (params.equalsIgnoreCase("remaining_uses")) {
            if (player.isOnline() && player.getPlayer() != null) {
                ItemStack item = player.getPlayer().getInventory().getItemInMainHand();
                if (plugin.getBlockManager().getUseTimes(item) > 0) {
                    return String.valueOf(plugin.getBlockManager().getUseTimes(item));
                }
            }
            return "0";
        }

        // 获取玩家是否持有魔法方块
        if (params.equalsIgnoreCase("has_block")) {
            if (player.isOnline() && player.getPlayer() != null) {
                ItemStack item = player.getPlayer().getInventory().getItemInMainHand();
                return String.valueOf(plugin.getBlockManager().isMagicBlock(item));
            }
            return "false";
        }

        // 获取玩家是否持有魔法食物
        if (params.equalsIgnoreCase("has_food")) {
            if (player.isOnline() && player.getPlayer() != null && plugin.getMagicFood() != null) {
                ItemStack item = player.getPlayer().getInventory().getItemInMainHand();
                return String.valueOf(plugin.getMagicFood().isMagicFood(item));
            }
            return "false";
        }

        // 获取玩家魔法方块的最大使用次数
        if (params.equalsIgnoreCase("max_uses")) {
            if (player.isOnline() && player.getPlayer() != null) {
                ItemStack item = player.getPlayer().getInventory().getItemInMainHand();
                if (plugin.getBlockManager().isMagicBlock(item)) {
                    return String.valueOf(plugin.getBlockManager().getMaxUseTimes(item));
                }
            }
            return "0";
        }

        // 获取玩家魔法方块的使用进度(百分比)
        if (params.equalsIgnoreCase("uses_progress")) {
            if (player.isOnline() && player.getPlayer() != null) {
                ItemStack item = player.getPlayer().getInventory().getItemInMainHand();
                if (plugin.getBlockManager().isMagicBlock(item)) {
                    int maxUses = plugin.getBlockManager().getMaxUseTimes(item);
                    int remainingUses = plugin.getBlockManager().getUseTimes(item);
                    if (maxUses > 0) {
                        double progress = ((double)(maxUses - remainingUses) / maxUses) * 100;
                        return String.format("%.1f", progress);
                    }
                }
            }
            return "0.0";
        }

        // 获取玩家魔法方块的进度条
        if (params.equalsIgnoreCase("progress_bar") || params.equalsIgnoreCase("progressbar")) {
            if (player.isOnline() && player.getPlayer() != null) {
                ItemStack item = player.getPlayer().getInventory().getItemInMainHand();
                if (plugin.getBlockManager().isMagicBlock(item)) {
                    int maxUses = plugin.getBlockManager().getMaxUseTimes(item);
                    int remainingUses = plugin.getBlockManager().getUseTimes(item);

                    // 检查是否是无限次数
                    if (maxUses == Integer.MAX_VALUE - 100) {
                        return "&a∞"; // 无限符号
                    }

                    if (maxUses > 0) {
                        // 使用插件的进度条生成方法
                        return plugin.getProgressBar(remainingUses, maxUses);
                    }
                }
            }
            return "&7无进度条"; // 默认返回空进度条
        }

        // 获取自定义长度的进度条
        if (params.startsWith("progress_bar_") || params.startsWith("progressbar_")) {
            try {
                // 从参数中提取进度条长度
                int barLength = Integer.parseInt(params.substring(params.lastIndexOf('_') + 1));
                if (barLength <= 0) barLength = 10; // 默认长度

                if (player.isOnline() && player.getPlayer() != null) {
                    ItemStack item = player.getPlayer().getInventory().getItemInMainHand();
                    if (plugin.getBlockManager().isMagicBlock(item)) {
                        int maxUses = plugin.getBlockManager().getMaxUseTimes(item);
                        int remainingUses = plugin.getBlockManager().getUseTimes(item);

                        // 检查是否是无限次数
                        if (maxUses == Integer.MAX_VALUE - 100) {
                            return "&a∞"; // 无限符号
                        }

                        if (maxUses > 0) {
                            // 生成自定义长度的进度条
                            double usedPercentage = (double) remainingUses / maxUses;
                            int filledBars = (int) Math.round(usedPercentage * barLength);

                            StringBuilder progressBar = new StringBuilder("&a");
                            for (int i = 0; i < barLength; i++) {
                                if (i < filledBars) {
                                    progressBar.append("■"); // 实心方块
                                } else {
                                    progressBar.append("□"); // 空心方块
                                }
                            }
                            return progressBar.toString();
                        }
                    }
                }
                return "&7" + "□".repeat(barLength); // 默认返回空进度条
            } catch (NumberFormatException e) {
                return "&c无效的进度条长度";
            }
        }

        return null;
    }
}