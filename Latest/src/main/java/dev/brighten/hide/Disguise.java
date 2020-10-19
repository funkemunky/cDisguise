package dev.brighten.hide;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MiscUtils;
import dev.brighten.hide.handler.SyncHandler;
import dev.brighten.hide.handler.VaultHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.shanerx.mojang.Mojang;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Disguise extends JavaPlugin {

    public static Disguise INSTANCE;
    public VaultHandler vaultHandler;
    public SyncHandler syncHandler;
    public ExecutorService disguiseThread;
    public Mojang mojang;

    public void onEnable() {
        saveDefaultConfig();
        bc("&7Loading cDisguise v" + getDescription().getVersion() + " by funkemunky...");
        INSTANCE = this;

        mojang = new Mojang().connect();

        enable("Atlas scanner");
        Atlas.getInstance().initializeScanner(this, true, true);

        Plugin vaultPlugin = Bukkit.getPluginManager().getPlugin("Vault");

        if(vaultPlugin != null) {
            enable("Vault hook");
            vaultHandler = new VaultHandler(vaultPlugin);
        } else bc("&cVault not installed on server. Cancelling hook...");

        enable("SyncHandler");
        syncHandler = new SyncHandler();
        disguiseThread = Executors.newSingleThreadExecutor();
    }

    public void onDisable() {
        disguiseThread.shutdown();
        HandlerList.unregisterAll(this);
        Atlas.getInstance().getEventManager().unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);
    }

    public static void bc(String msg) {
        MiscUtils.printToConsole(msg);
    }

    private static void enable(String msg) {
        bc(Color.Red + "Enabling " + msg + "...");
    }

    private static void disable(String msg) {
        bc(Color.Red + "Disabling " + msg + "...");
    }
}
