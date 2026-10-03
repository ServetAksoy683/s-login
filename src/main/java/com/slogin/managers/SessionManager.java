package com.slogin.managers;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Sunucu calisirken bellekte tutulan, kalici olmayan oturum verileri.
 * Sunucu yeniden baslayinca (dogal olarak) sifirlanir; hesap verileri AccountManager'da kalicidir.
 */
public class SessionManager {

    public record PendingChange(String newPassword, boolean wasAuthenticatedBefore) {
    }

    private final Set<UUID> authenticated = new HashSet<>();
    private final Map<UUID, Integer> failedAttempts = new HashMap<>();
    private final Map<UUID, PendingChange> pendingChanges = new HashMap<>();
    private final Map<UUID, Location> freezeAnchors = new HashMap<>();
    private final Map<UUID, ScheduledTask> timerTasks = new HashMap<>();
    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    // ---------- Giris durumu ----------

    public boolean isAuthenticated(UUID uuid) {
        return authenticated.contains(uuid);
    }

    public void setAuthenticated(UUID uuid, boolean value) {
        if (value) {
            authenticated.add(uuid);
        } else {
            authenticated.remove(uuid);
        }
    }

    // ---------- Yanlis deneme sayaci ----------

    public int incrementFailedAttempts(UUID uuid) {
        int next = failedAttempts.getOrDefault(uuid, 0) + 1;
        failedAttempts.put(uuid, next);
        return next;
    }

    public void resetFailedAttempts(UUID uuid) {
        failedAttempts.remove(uuid);
    }

    // ---------- Bekleyen sifre degisikligi ----------

    public void setPendingChange(UUID uuid, PendingChange change) {
        pendingChanges.put(uuid, change);
    }

    public PendingChange getPendingChange(UUID uuid) {
        return pendingChanges.get(uuid);
    }

    public void clearPendingChange(UUID uuid) {
        pendingChanges.remove(uuid);
    }

    // ---------- Hareket dondurma (freeze) ----------

    public void setFreezeAnchor(UUID uuid, Location location) {
        freezeAnchors.put(uuid, location);
    }

    public Location getFreezeAnchor(UUID uuid) {
        return freezeAnchors.get(uuid);
    }

    public void clearFreezeAnchor(UUID uuid) {
        freezeAnchors.remove(uuid);
    }

    // ---------- Sayac gorevi (timer task) ----------

    public void setTimerTask(UUID uuid, ScheduledTask task) {
        timerTasks.put(uuid, task);
    }

    public void cancelTimerTask(UUID uuid) {
        ScheduledTask task = timerTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }

    // ---------- Boss bar ----------

    public void setBossBar(UUID uuid, BossBar bar) {
        bossBars.put(uuid, bar);
    }

    public BossBar getBossBar(UUID uuid) {
        return bossBars.get(uuid);
    }

    public void clearBossBar(UUID uuid) {
        bossBars.remove(uuid);
    }

    /**
     * Oyuncu ayrildiginda ya da giris tamamlandiginda tum gecici veriyi temizler (hesap verisine dokunmaz).
     */
    public void clearAll(UUID uuid) {
        authenticated.remove(uuid);
        failedAttempts.remove(uuid);
        pendingChanges.remove(uuid);
        freezeAnchors.remove(uuid);
        cancelTimerTask(uuid);
        bossBars.remove(uuid);
    }
}
