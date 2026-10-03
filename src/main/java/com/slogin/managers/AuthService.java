package com.slogin.managers;

import com.slogin.SLogin;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

/**
 * Oyuncuyu giris yapana kadar "kilitler": ekranini karartir, yerinde dondurur
 * ve ustte geri sayan bir boss bar gosterir. Sure dolarsa atar.
 */
public class AuthService {

    private final SLogin plugin;

    public AuthService(SLogin plugin) {
        this.plugin = plugin;
    }

    public void lock(Player player) {
        UUID uuid = player.getUniqueId();
        SessionManager session = plugin.sessions();

        session.setAuthenticated(uuid, false);

        if (plugin.getConfig().getBoolean("blind-effect", true)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0, true, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, Integer.MAX_VALUE, 0, true, false, false));
        }

        if (plugin.getConfig().getBoolean("freeze-movement", true)) {
            session.setFreezeAnchor(uuid, player.getLocation());
        }

        startTimer(player);
    }

    private void startTimer(Player player) {
        UUID uuid = player.getUniqueId();
        SessionManager session = plugin.sessions();

        int totalSeconds = plugin.getConfig().getInt("login-timeout-seconds", 120);
        int[] remaining = {totalSeconds};

        BossBar bar = BossBar.bossBar(
                plugin.lang().get("timer-title", "%seconds%", String.valueOf(remaining[0])),
                1.0f,
                BossBar.Color.RED,
                BossBar.Overlay.PROGRESS
        );
        player.showBossBar(bar);
        session.setBossBar(uuid, bar);

        ScheduledTask task = player.getScheduler().runAtFixedRate(plugin, scheduledTask -> {
            if (session.isAuthenticated(uuid)) {
                scheduledTask.cancel();
                return;
            }

            remaining[0]--;

            if (remaining[0] <= 0) {
                removeLockEffects(player);
                player.kick(plugin.lang().get("kicked-timeout"));
                return;
            }

            float progress = Math.max(0f, Math.min(1f, remaining[0] / (float) totalSeconds));
            bar.progress(progress);
            bar.name(plugin.lang().get("timer-title", "%seconds%", String.valueOf(remaining[0])));
            player.sendActionBar(plugin.lang().get(hintKeyFor(uuid)));

            String soundKey = remaining[0] <= 10 ? "tick-urgent" : "tick";
            plugin.sounds().play(player, soundKey);
        }, () -> {
        }, 20L, 20L);

        session.setTimerTask(uuid, task);
    }

    /**
     * Giris/kayit/onay basarili oldugunda cagrilir: kilidi tamamen kaldirir.
     */
    public void unlock(Player player) {
        UUID uuid = player.getUniqueId();
        removeLockEffects(player);
        plugin.sessions().setAuthenticated(uuid, true);
        plugin.sessions().resetFailedAttempts(uuid);
    }

    private String hintKeyFor(UUID uuid) {
        if (!plugin.accounts().hasAccount(uuid)) {
            return "register-hint";
        }
        if (plugin.accounts().needsNewPassword(uuid)) {
            return "reset-hint";
        }
        return "login-hint";
    }

    private void removeLockEffects(Player player) {
        UUID uuid = player.getUniqueId();
        SessionManager session = plugin.sessions();

        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.DARKNESS);

        session.clearFreezeAnchor(uuid);

        BossBar bar = session.getBossBar(uuid);
        if (bar != null) {
            player.hideBossBar(bar);
            session.clearBossBar(uuid);
        }

        session.cancelTimerTask(uuid);
    }
}
