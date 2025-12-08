package io.github.syferie.magicblock.food;

import io.github.syferie.magicblock.MagicBlockPlugin;
import io.github.syferie.magicblock.api.IMagicFood;
import io.github.syferie.magicblock.core.AbstractMagicItem;
import io.github.syferie.magicblock.util.LoreUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

/**
 * 食物管理器 - 重构后仅保留食物特有逻辑
 *
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public class FoodManager extends AbstractMagicItem implements Listener, IMagicFood {

    /**
     * 构造函数
     *
     * @param plugin 插件实例
     */
    public FoodManager(MagicBlockPlugin plugin) {
        super(plugin, "magicfood"); // keyPrefix: magicfood_times, magicfood_maxtimes
    }

    // ==================== 食物创建 ====================

    @Override
    public ItemStack createMagicFood(Material material) {
        if (!plugin.getFoodConfig().contains("foods." + material.name())) {
            return null;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        ConfigurationSection foodSection = plugin.getFoodConfig()
            .getConfigurationSection("foods." + material.name());
        if (foodSection == null) return null;

        // 设置显示名称
        String nameFormat = plugin.getFoodConfig()
            .getString("display.name-format", "&b✦ %s &b✦");
        String materialName = plugin.getMinecraftLangManager().getItemStackName(item);
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
            String.format(nameFormat, materialName)));

        // 添加附魔和隐藏标记
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        // 设置使用次数
        int useTimes = foodSection.getInt("use-times", plugin.getDefaultBlockTimes());
        item.setItemMeta(meta);
        setUseTimes(item, useTimes);

        return item;
    }

    // ==================== 食物识别 ====================

    public boolean isMagicFood(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        String specialLore = plugin.getFoodConfig()
            .getString("special-lore", "§7MagicFood");
        List<String> lore = item.getItemMeta().getLore();

        if (lore == null) return false;
        return lore.contains(specialLore);
    }

    @Override
    public boolean isMagicItem(ItemStack item) {
        return isMagicFood(item);
    }

    @Override
    public String getMagicItemType() {
        return "FOOD";
    }

    // ==================== 模板方法实现 ====================

    @Override
    protected String getMagicLoreIdentifier() {
        return plugin.getFoodConfig().getString("special-lore", "§7MagicFood");
    }

    @Override
    protected List<String> getDecorativeLore(ItemStack item, Player owner) {
        List<String> configLore = plugin.getFoodConfig()
            .getStringList("display.decorative-lore.lines");

        // 食物特有: 替换食物属性变量
        ConfigurationSection foodSection = plugin.getFoodConfig()
            .getConfigurationSection("foods." + item.getType().name());

        if (foodSection != null) {
            List<String> processedLore = new ArrayList<>();
            for (String line : configLore) {
                line = line.replace("%magicfood_food_level%",
                        String.valueOf(foodSection.getInt("food-level", 0)))
                       .replace("%magicfood_saturation%",
                        String.valueOf(foodSection.getDouble("saturation", 0.0)))
                       .replace("%magicfood_heal%",
                        String.valueOf(foodSection.getDouble("heal", 0.0)));
                processedLore.add(line);
            }
            configLore = processedLore;
        }

        return LoreUtil.processDecorativeLoreList(configLore, owner);
    }

    @Override
    protected String getUsageLorePrefix() {
        return plugin.getFoodConfig()
            .getString("display.lore-text.uses-label", "Uses:");
    }

    @Override
    protected boolean shouldShowBinding() {
        return false; // 食物不显示绑定信息
    }

    // ==================== 食物效果应用 ====================

    /**
     * 应用食物效果 (饱食度、生命值、药水效果、音效、粒子)
     */
    private void applyFoodEffects(Player player, Material foodType) {
        ConfigurationSection foodSection = plugin.getFoodConfig()
            .getConfigurationSection("foods." + foodType.name());
        if (foodSection == null) return;

        // 恢复饥饿值和饱食度
        int foodLevel = foodSection.getInt("food-level", 0);
        float saturation = (float) foodSection.getDouble("saturation", 0.0);
        double heal = foodSection.getDouble("heal", 0.0);

        int newFoodLevel = Math.min(player.getFoodLevel() + foodLevel, 20);
        player.setFoodLevel(newFoodLevel);
        player.setSaturation(Math.min(player.getSaturation() + saturation, 20.0f));

        // 恢复生命值
        if (heal > 0) {
            double maxHealth = player.getAttribute(
                org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
            double newHealth = Math.min(player.getHealth() + heal, maxHealth);
            player.setHealth(newHealth);
        }

        // 应用药水效果
        applyPotionEffects(player, foodSection);

        // 播放音效
        playSoundEffect(player);

        // 显示粒子效果
        spawnParticleEffect(player);
    }

    /**
     * 应用药水效果
     */
    private void applyPotionEffects(Player player, ConfigurationSection foodSection) {
        ConfigurationSection effectsSection = foodSection.getConfigurationSection("effects");
        if (effectsSection == null) return;

        for (String effectName : effectsSection.getKeys(false)) {
            PotionEffectType effectType = PotionEffectType.getByName(effectName);
            if (effectType != null) {
                ConfigurationSection effectSection = effectsSection
                    .getConfigurationSection(effectName);
                if (effectSection != null) {
                    int duration = effectSection.getInt("duration", 200);
                    int amplifier = effectSection.getInt("amplifier", 0);
                    player.addPotionEffect(new PotionEffect(effectType, duration, amplifier));
                }
            }
        }
    }

    /**
     * 播放音效
     */
    private void playSoundEffect(Player player) {
        if (!plugin.getFoodConfig().getBoolean("sound.enabled", true)) return;

        String soundName = plugin.getFoodConfig().getString("sound.eat", "ENTITY_PLAYER_BURP");
        float volume = (float) plugin.getFoodConfig().getDouble("sound.volume", 1.0);
        float pitch = (float) plugin.getFoodConfig().getDouble("sound.pitch", 1.0);

        try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound: " + soundName);
        }
    }

    /**
     * 显示粒子效果
     */
    private void spawnParticleEffect(Player player) {
        if (!plugin.getFoodConfig().getBoolean("particles.enabled", true)) return;

        String particleType = plugin.getFoodConfig().getString("particles.type", "HEART");
        int count = plugin.getFoodConfig().getInt("particles.count", 5);
        double spreadX = plugin.getFoodConfig().getDouble("particles.spread.x", 0.5);
        double spreadY = plugin.getFoodConfig().getDouble("particles.spread.y", 0.5);
        double spreadZ = plugin.getFoodConfig().getDouble("particles.spread.z", 0.5);

        try {
            Particle particle = Particle.valueOf(particleType);
            player.getWorld().spawnParticle(particle,
                player.getLocation().add(0, 1, 0),
                count, spreadX, spreadY, spreadZ);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid particle: " + particleType);
        }
    }

    // ==================== 食物消耗事件处理 ====================

    @EventHandler
    public void onPlayerEat(PlayerItemConsumeEvent event) {
        ItemStack originalItem = event.getItem();
        if (!isMagicFood(originalItem)) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        // 检查是否允许在饱食度满时使用
        if (!plugin.getFoodConfig().getBoolean("allow-use-when-full", true)
            && player.getFoodLevel() >= 20) {
            plugin.sendMessage(player, "messages.food-full");
            return;
        }

        // 创建物品副本
        ItemStack item = originalItem.clone();

        // 检查使用次数
        int currentTimes = getUseTimes(item);
        if (currentTimes <= 0) {
            removeItemFromHand(player, EquipmentSlot.HAND);
            return;
        }

        // 应用食物效果
        applyFoodEffects(player, item.getType());
        if (plugin.getStatistics() != null) {
            plugin.getStatistics().logFoodUse(player, item);
        }

        // 减少使用次数 (基类处理)
        currentTimes = decrementUseTimes(item);

        // 更新手持物品
        if (currentTimes <= 0) {
            removeItemFromHand(player, EquipmentSlot.HAND);
            plugin.sendMessage(player, "messages.food-removed");
        } else {
            updateItemInHand(player, item, EquipmentSlot.HAND);
        }
    }

    /**
     * 移除手持物品
     */
    private void removeItemFromHand(Player player, EquipmentSlot slot) {
        if (slot == EquipmentSlot.HAND) {
            player.getInventory().setItemInMainHand(null);
        } else {
            player.getInventory().setItemInOffHand(null);
        }
    }

    /**
     * 更新手持物品
     */
    private void updateItemInHand(Player player, ItemStack item, EquipmentSlot slot) {
        if (slot == EquipmentSlot.HAND) {
            player.getInventory().setItemInMainHand(item);
        } else {
            player.getInventory().setItemInOffHand(item);
        }
    }
}
