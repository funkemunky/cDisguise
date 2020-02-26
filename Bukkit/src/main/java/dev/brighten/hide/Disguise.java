package dev.brighten.hide;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MiscUtils;
import dev.brighten.hide.handler.SyncHandler;
import dev.brighten.hide.handler.VaultHandler;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Disguise extends JavaPlugin {

    public static Disguise INSTANCE;
    public VaultHandler vaultHandler;
    public SyncHandler syncHandler;

    public void onEnable() {
        bc("&7Loading cDisguise v" + getDescription().getVersion() + " by funkemunky...");
        INSTANCE = this;

        Plugin vaultPlugin = Bukkit.getPluginManager().getPlugin("Vault");

        if(vaultPlugin != null) {
            enable("Vault hook");
            vaultHandler = new VaultHandler(vaultPlugin);
        } else bc("&cVault not installed on server. Cancelling hook...");

        enable("Atlas scanner");
        Atlas.getInstance().initializeScanner(this, true, true);

        enable("SyncHandler");
        syncHandler = new SyncHandler();
    }

    public void onDisable() {

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
