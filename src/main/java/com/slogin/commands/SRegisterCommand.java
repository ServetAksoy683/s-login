package com.slogin.commands;

import com.slogin.SLogin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SRegisterCommand implements CommandExecutor {

    private final SLogin plugin;

    public SRegisterCommand(SLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.lang().get("only-players"));
            return true;
        }

        if (plugin.accounts().hasAccount(player.getUniqueId())) {
            player.sendMessage(plugin.lang().get("already-registered"));
            plugin.sounds().play(player, "error");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.lang().get("usage-register"));
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

        plugin.accounts().register(player.getUniqueId(), player.getName(), args[0]);
        plugin.auth().unlock(player);

        player.sendMessage(plugin.lang().get("register-success"));
        plugin.sounds().play(player, "register-success");

        return true;
    }
}
