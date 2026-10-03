package com.slogin.commands;

import com.slogin.SLogin;
import com.slogin.managers.SessionManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SChangeCommand implements CommandExecutor {

    private final SLogin plugin;

    public SChangeCommand(SLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.lang().get("only-players"));
            return true;
        }

        boolean authenticated = plugin.sessions().isAuthenticated(player.getUniqueId());
        boolean resetFlow = !authenticated
                && plugin.accounts().hasAccount(player.getUniqueId())
                && plugin.accounts().needsNewPassword(player.getUniqueId());

        if (!authenticated && !resetFlow) {
            if (!plugin.accounts().hasAccount(player.getUniqueId())) {
                player.sendMessage(plugin.lang().get("not-registered"));
            } else {
                player.sendMessage(plugin.lang().get("must-login-first"));
            }
            plugin.sounds().play(player, "error");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.lang().get("usage-change"));
            return true;
        }

        if (!args[0].equals(args[1])) {
            player.sendMessage(plugin.lang().get("passwords-dont-match"));
            plugin.sounds().play(player, "error");
            return true;
        }

        int minLength = plugin.getConfig().getInt("min-password-length", 4);
        if (args[0].length() < minLength) {
            player.sendMessage(plugin.lang().get("password-too-short", "%min%", String.valueOf(minLength)));
            plugin.sounds().play(player, "error");
            return true;
        }

        plugin.sessions().setPendingChange(
                player.getUniqueId(),
                new SessionManager.PendingChange(args[0], authenticated)
        );

        player.sendMessage(plugin.lang().get("change-pending"));
        plugin.sounds().play(player, "confirm-needed");

        return true;
    }
}
