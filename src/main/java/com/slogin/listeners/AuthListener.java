package com.slogin.listeners;

import com.slogin.SLogin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class AuthListener implements Listener {

    // Giris yapmamis bir oyuncunun kullanmasina izin verilen komutlar.
    private static final Set<String> ALLOWED_COMMANDS = Set.of(
            "/sregister", "/slogin", "/schange", "/sconfirm"
    );

    private final SLogin plugin;

    public AuthListener(SLogin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        plugin.auth().lock(player);

        if (!plugin.accounts().hasAccount(uuid)) {
            player.sendMessage(plugin.lang().get("register-prompt"));
        } else if (plugin.accounts().needsNewPassword(uuid)) {
            player.sendMessage(plugin.lang().get("reset-prompt"));
        } else {
            player.sendMessage(plugin.lang().get("login-prompt"));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.sessions().clearAll(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!plugin.getConfig().getBoolean("freeze-movement", true)) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (plugin.sessions().isAuthenticated(uuid)) {
            return;
        }

        Location anchor = plugin.sessions().getFreezeAnchor(uuid);
        if (anchor == null) {
            return;
        }

        Location to = event.getTo();
        if (to == null) {
            return;
        }

        if (anchor.getBlockX() != to.getBlockX()
                || anchor.getBlockY() != to.getBlockY()
                || anchor.getBlockZ() != to.getBlockZ()) {
            event.setTo(anchor);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!plugin.getConfig().getBoolean("prevent-damage", true)) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!plugin.sessions().isAuthenticated(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!plugin.getConfig().getBoolean("block-chat-and-commands", true)) {
            return;
        }

        Player player = event.getPlayer();
        if (plugin.sessions().isAuthenticated(player.getUniqueId())) {
            return;
        }

        String fullCommand = event.getMessage().toLowerCase(Locale.ROOT);
        String base = fullCommand.split(" ")[0];

        if (ALLOWED_COMMANDS.contains(base)) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(plugin.lang().get("must-login-first"));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!plugin.getConfig().getBoolean("block-chat-and-commands", true)) {
            return;
        }

        Player player = event.getPlayer();
        if (plugin.sessions().isAuthenticated(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(plugin.lang().get("must-login-first"));
    }
}
