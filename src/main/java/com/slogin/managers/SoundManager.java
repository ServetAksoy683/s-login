package com.slogin.managers;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SoundManager {

    private final JavaPlugin plugin;

    public SoundManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void play(Player player, String key) {
        if (player == null || !player.isOnline()) {
            return;
        }

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("sounds." + key);
        if (section == null) {
            return;
        }

        String soundName = section.getString("sound", "");
        if (soundName == null || soundName.isBlank()) {
            return;
        }

        try {
            Sound sound = Sound.valueOf(soundName.trim().toUpperCase());
            float volume = (float) section.getDouble("volume", 1.0);
            float pitch = (float) section.getDouble("pitch", 1.0);
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("Gecersiz ses ismi ('" + soundName + "') -> sounds." + key);
        }
    }
}
