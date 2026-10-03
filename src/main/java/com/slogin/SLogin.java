package com.slogin;

import com.slogin.commands.SChangeCommand;
import com.slogin.commands.SConfirmCommand;
import com.slogin.commands.SLoginCommand;
import com.slogin.commands.SPasswordResetCommand;
import com.slogin.commands.SRegisterCommand;
import com.slogin.listeners.AuthListener;
import com.slogin.managers.AccountManager;
import com.slogin.managers.AuthService;
import com.slogin.managers.LangManager;
import com.slogin.managers.SessionManager;
import com.slogin.managers.SoundManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class SLogin extends JavaPlugin {

    private LangManager langManager;
    private SoundManager soundManager;
    private AccountManager accountManager;
    private SessionManager sessionManager;
    private AuthService authService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.langManager = new LangManager(this);
        this.soundManager = new SoundManager(this);
        this.accountManager = new AccountManager(this);
        this.sessionManager = new SessionManager();
        this.authService = new AuthService(this);

        getServer().getPluginManager().registerEvents(new AuthListener(this), this);

        getCommand("sregister").setExecutor(new SRegisterCommand(this));
        getCommand("slogin").setExecutor(new SLoginCommand(this));
        getCommand("spasswordreset").setExecutor(new SPasswordResetCommand(this));
        getCommand("schange").setExecutor(new SChangeCommand(this));
        getCommand("sconfirm").setExecutor(new SConfirmCommand(this));

        getLogger().info("S-Login aktif edildi.");
    }

    @Override
    public void onDisable() {
        if (accountManager != null) {
            accountManager.save();
        }
        getLogger().info("S-Login devre disi birakildi.");
    }

    public LangManager lang() {
        return langManager;
    }

    public SoundManager sounds() {
        return soundManager;
    }

    public AccountManager accounts() {
        return accountManager;
    }

    public SessionManager sessions() {
        return sessionManager;
    }

    public AuthService auth() {
        return authService;
    }
}
