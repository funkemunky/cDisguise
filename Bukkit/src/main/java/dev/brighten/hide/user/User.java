package dev.brighten.hide.user;

import dev.brighten.db.utils.json.JSONObject;
import dev.brighten.hide.Disguise;
import dev.brighten.hide.disguise.DisguiseObject;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class User {
    /** Static instances **/
    private static Map<UUID, User> userMap = new ConcurrentHashMap<>();

    public static User getUser(UUID uuid) {
        return userMap.computeIfAbsent(uuid, key -> {
            User user = new User(key);

            userMap.put(key, user);

            return user;
        });
    }

    /** Object oriented instances **/

    public final UUID uuid;
    public boolean disguised;
    private Player player;
    @Setter
    private String originalGroup = "";
    public DisguiseObject disguise;

    public User(UUID uuid) {
        this.uuid = uuid;

        if(Disguise.INSTANCE.vaultHandler != null) {
            originalGroup = Disguise.INSTANCE.vaultHandler.permission.getPrimaryGroup(getPlayer());
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
        object.put("disguised", disguised);
        object.put("group", getOriginalGroup());
        object.put("disguise", disguise != null ? disguise.toJson() : "");

        return object.toString();
    }
}
