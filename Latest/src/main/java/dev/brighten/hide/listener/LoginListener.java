package dev.brighten.hide.listener;

import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import dev.brighten.hide.Disguise;
import dev.brighten.hide.handler.DisguiseHandler;
import dev.brighten.hide.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@Init
public class LoginListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent event) {
        Disguise.INSTANCE.disguiseThread.execute(() -> {
            User user = User.getUser(event.getPlayer());

            if(user.disguise != null) {
                if(user.disguise.isActive()) {
                    DisguiseHandler.INSTANCE.disguisePlayer(event.getPlayer(),
                            user.disguise.playerName, user.disguise.groupDisguise);
                    event.getPlayer().sendMessage(Color.translate("&7Your previous disguise as &e"
                            + user.disguise.playerName + "&7has been applied."));
                } else if(!user.disguise.groupDisguise.equals(user.originalPrefix)) {
                    Disguise.INSTANCE.vaultHandler.chat.setPlayerPrefix(event.getPlayer(), user.originalPrefix);
                }
            }
        });
    }
}
