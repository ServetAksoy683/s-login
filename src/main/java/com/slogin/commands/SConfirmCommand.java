package com.slogin.commands;

import com.slogin.SLogin;
import com.slogin.managers.SessionManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SConfirmCommand implements CommandExecutor {

    private final SLogin plugin;

    public SConfirmCommand(SLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.lang().get("only-players"));
            return true;
        }

        SessionManager.PendingChange pending = plugin.sessions().getPendingChange(player.getUniqueId());
        if (pending == null) {
            player.sendMessage(plugin.lang().get("confirm-nothing-pending"));
            plugin.sounds().play(player, "error");
            return true;
        }

        plugin.accounts().updatePassword(player.getUniqueId(), pending.newPassword());
        plugin.sessions().clearPendingChange(player.getUniqueId());
        plugin.sounds().play(player, "confirmed");

        if (pending.wasAuthenticatedBefore()) {
            player.sendMessage(plugin.lang().get("confirm-success"));
        } else {
            // Sifre sifirlama akisindan geldi: oyuncu hala kilitli (giris yapmadi),
            // yeni sifresiyle tekrar /slogin yapmasi gerekiyor.
            player.sendMessage(plugin.lang().get("confirm-success-relogin"));
        }

        return true;
    }
}
