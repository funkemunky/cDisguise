package dev.brighten.hide.user;

import dev.brighten.db.utils.json.JSONObject;
import dev.brighten.hide.Disguise;
import dev.brighten.hide.disguise.DisguiseObject;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class User {
    /** Static instances **/
    private static Map<Player, User> userMap = new HashMap<>();

    public static User getUser(Player player) {
        return userMap.computeIfAbsent(player, key -> {
            User user = new User(key);

            userMap.put(key, user);
            return user;
        });
    }

    /** Object oriented instances **/

    public final UUID uuid;
    private Player player;
    public String originalPrefix, orignalName;
    @Setter
    private String originalGroup;
    public DisguiseObject disguise;
    public String rankOption, nameOption;

    public User(Player player) {
        this.uuid = player.getUniqueId();
        this.player = player;

        orignalName = player.getName();
        if(Disguise.INSTANCE.vaultHandler != null) {
            originalPrefix = Disguise.INSTANCE.vaultHandler.chat.getPlayerPrefix(getPlayer());
        }

        //Updating user to see if anything was stored.
        Disguise.INSTANCE.syncHandler.updateUser(this);
    }

    public Player getPlayer() {
        if(player == null) {
            return this.player = Bukkit.getPlayer(uuid);
        }
        return this.player;
    }

    public String getOriginalGroup() {
        if(originalGroup.length() == 0) {
            if(Disguise.INSTANCE.vaultHandler != null) {
                originalGroup = Disguise.INSTANCE.vaultHandler.permission.getPrimaryGroup(getPlayer());
            }
        }
        return originalGroup;
    }

    @SneakyThrows
    public String toJson() {
        JSONObject object = new JSONObject();

        object.put("uuid", uuid.toString());
        object.put("group", getOriginalGroup());
        object.put("disguise", disguise != null ? disguise.toJson() : "");

        return object.toString();
    }
}
