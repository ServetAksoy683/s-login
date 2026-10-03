package com.slogin.commands;

import com.slogin.SLogin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SPasswordResetCommand implements CommandExecutor {

    private final SLogin plugin;

    public SPasswordResetCommand(SLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player && !player.isOp() && !player.hasPermission("slogin.admin")) {
            sender.sendMessage(plugin.lang().get("no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.lang().get("usage-reset"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(plugin.lang().get("player-not-found", "%player%", args[0]));
            return true;
        }

        if (!plugin.accounts().hasAccount(target.getUniqueId())) {
            sender.sendMessage(plugin.lang().get("not-registered"));
            return true;
        }

        plugin.accounts().markNeedsNewPassword(target.getUniqueId());

        // Hedef oyuncu zaten oturum actiysa, onu tekrar kilitleyip sifre degistirmeye zorla.
        plugin.sessions().setAuthenticated(target.getUniqueId(), false);
        plugin.auth().lock(target);
        target.sendMessage(plugin.lang().get("reset-player-notify"));
        target.sendMessage(plugin.lang().get("reset-prompt"));

        sender.sendMessage(plugin.lang().get("reset-admin-success", "%player%", target.getName()));
        if (sender instanceof Player adminPlayer) {
            plugin.sounds().play(adminPlayer, "admin-action");
        }

        return true;
    }
}
