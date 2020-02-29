package dev.brighten.hide.handler;

import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import dev.brighten.hide.Disguise;
import dev.brighten.hide.user.User;
import me.activated.core.plugin.AquaCoreAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

@Init(requirePlugins = {"AquaCore"})
public class AquaHandler {
    public static AquaHandler INSTANCE;

    @ConfigSetting(name = "chatFormat")
    private static String chatFormat = "<prefix><namecolor><player><suffix><tag>&8: &f<message>";

    @ConfigSetting(path = "aquacore", name = "prefixes")
    private static boolean usePrefix = false;

    @ConfigSetting(path = "aquacore", name = "suffixes")
    private static boolean useSuffix = false;

    public AquaHandler() {
        INSTANCE = this;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsync(AsyncPlayerChatEvent event) {
        User user = User.getUser(event.getPlayer());

        if(user.disguise != null && user.disguise.isActive()) {
            String prefix = Disguise.INSTANCE.vaultHandler.chat.getPlayerPrefix(event.getPlayer()),
                    suffix = Disguise.INSTANCE.vaultHandler.chat.getPlayerSuffix(event.getPlayer());

            String chatFormat = AquaHandler.chatFormat;

            chatFormat = chatFormat.replace("<prefix>", prefix);
            chatFormat = chatFormat.replace("<namecolor>",
                    AquaCoreAPI.INSTANCE.getPlayerNameColor(event.getPlayer().getUniqueId()).toString());
            chatFormat = chatFormat.replace("<player>", event.getPlayer().getDisplayName());
            chatFormat = chatFormat.replace("<suffix>", suffix);
            chatFormat = chatFormat.replace("<tag>", AquaCoreAPI.INSTANCE.getTagFormat(event.getPlayer()));

            event.setFormat(Color.translate(chatFormat).replace("<message>", event.getMessage()));
        }
    }
}
