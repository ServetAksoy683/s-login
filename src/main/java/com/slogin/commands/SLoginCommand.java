package com.slogin.commands;

import com.slogin.SLogin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SLoginCommand implements CommandExecutor {

    private final SLogin plugin;

    public SLoginCommand(SLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.lang().get("only-players"));
            return true;
        }

        if (!plugin.accounts().hasAccount(player.getUniqueId())) {
            player.sendMessage(plugin.lang().get("not-registered"));
            plugin.sounds().play(player, "error");
            return true;
        }

        if (plugin.accounts().needsNewPassword(player.getUniqueId())) {
            player.sendMessage(plugin.lang().get("reset-prompt"));
            plugin.sounds().play(player, "error");
            return true;
        }

        if (plugin.sessions().isAuthenticated(player.getUniqueId())) {
            player.sendMessage(plugin.lang().get("already-logged-in"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(plugin.lang().get("usage-login"));
            return true;
        }

        if (plugin.accounts().checkPassword(player.getUniqueId(), args[0])) {
            plugin.auth().unlock(player);
            player.sendMessage(plugin.lang().get("login-success"));
            plugin.sounds().play(player, "login-success");
            return true;
        }

        int maxAttempts = plugin.getConfig().getInt("max-failed-attempts", 4);
        int attempts = plugin.sessions().incrementFailedAttempts(player.getUniqueId());

        if (attempts >= maxAttempts) {
            plugin.sounds().play(player, "kicked");
            player.kick(plugin.lang().get("kicked-wrong-password"));
            return true;
        }

        player.sendMessage(plugin.lang().get(
                "wrong-password",
                "%attempts%", String.valueOf(attempts),
                "%max%", String.valueOf(maxAttempts)
        ));
        plugin.sounds().play(player, "wrong-password");

        return true;
    }
}
