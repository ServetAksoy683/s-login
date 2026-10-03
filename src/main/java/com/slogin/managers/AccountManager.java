package com.slogin.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * accounts.yml icinde her oyuncu icin: isim, tuz (salt) ve sifre hash'i tutar.
 * Sifreler asla duz metin olarak saklanmaz; SHA-256 + rastgele tuz kullanilir.
 */
public class AccountManager {

    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration data;
    private final SecureRandom random = new SecureRandom();

    public AccountManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "accounts.yml");
        load();
    }

    private void load() {
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            data = new YamlConfiguration();
            return;
        }
        data = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            plugin.getDataFolder().mkdirs();
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("accounts.yml kaydedilemedi: " + e.getMessage());
        }
    }

    public boolean hasAccount(UUID uuid) {
        return data.contains("accounts." + uuid);
    }

    public boolean needsNewPassword(UUID uuid) {
        return data.getBoolean("accounts." + uuid + ".needs-new-password", false);
    }

    public void register(UUID uuid, String name, String password) {
        String salt = generateSalt();
        String hash = hash(password, salt);

        data.set("accounts." + uuid + ".name", name);
        data.set("accounts." + uuid + ".salt", salt);
        data.set("accounts." + uuid + ".hash", hash);
        data.set("accounts." + uuid + ".needs-new-password", false);
        save();
    }

    public boolean checkPassword(UUID uuid, String password) {
        String salt = data.getString("accounts." + uuid + ".salt");
        String storedHash = data.getString("accounts." + uuid + ".hash");
        if (salt == null || storedHash == null) {
            return false;
        }
        return hash(password, salt).equals(storedHash);
    }

    /**
     * Sifreyi kalici olarak degistirir (onay sonrasi cagrilir).
     */
    public void updatePassword(UUID uuid, String newPassword) {
        String salt = generateSalt();
        String hash = hash(newPassword, salt);

        data.set("accounts." + uuid + ".salt", salt);
        data.set("accounts." + uuid + ".hash", hash);
        data.set("accounts." + uuid + ".needs-new-password", false);
        save();
    }

    /**
     * Admin bir oyuncunun sifresini sifirlar: bir dahaki girise kadar o oyuncu /schange yapmak zorunda kalir.
     */
    public void markNeedsNewPassword(UUID uuid) {
        data.set("accounts." + uuid + ".needs-new-password", true);
        save();
    }

    private String generateSalt() {
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return bytesToHex(bytes);
    }

    private String hash(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes());
            byte[] hashed = digest.digest(password.getBytes());
            return bytesToHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algoritmasi bulunamadi", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
