package dev.brighten.hide.handler;

import cc.funkemunky.api.utils.MiscUtils;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Arrays;
import java.util.List;

public class VaultHandler {

    public Permission permission;
    public Chat chat;
    public Plugin vaultPlugin;

    public VaultHandler(Plugin vaultPlugin) {
        this.vaultPlugin = vaultPlugin;
        if(setupPermissions())
            MiscUtils.printToConsole("&aSuccessfully hooked into Vault Permissions v"
                    + vaultPlugin.getDescription().getVersion() + "!");
        if(setupChat()) {
            MiscUtils.printToConsole("&aSuccessfully hooked into Vault Chat v"
                    + vaultPlugin.getDescription().getVersion() + "!");
        }
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = Bukkit.getServicesManager().getRegistration(Permission.class);
        permission = rsp.getProvider();
        return permission != null;
    }
    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = Bukkit.getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }
    public List<String> getRanks() {
        return Arrays.asList(permission.getGroups());
    }
}
