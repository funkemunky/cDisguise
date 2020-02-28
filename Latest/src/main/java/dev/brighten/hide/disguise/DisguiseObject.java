package dev.brighten.hide.disguise;

import dev.brighten.db.utils.json.JSONObject;
import dev.brighten.hide.handler.DisguiseHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

@RequiredArgsConstructor
@AllArgsConstructor
public class DisguiseObject {
    public final UUID uuid;
    public final String playerName;
    public final String groupDisguise;
    @Getter
    private boolean active;

    @SneakyThrows
    public String toJson() {
        JSONObject object = new JSONObject();

        object.put("uuid", uuid.toString());
        object.put("name", playerName);
        object.put("group", groupDisguise);
        object.put("active", active);

        return object.toString();
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

    @SneakyThrows
    public static DisguiseObject fromJson(String json) {
        JSONObject object = new JSONObject(json);

        return new DisguiseObject(UUID.fromString(object.getString("uuid")),
                object.getString("name"), object.getString("group"), object.getBoolean("active"));
    }

    public void setActive(boolean active) {
        boolean wasActive = this.active;
        if(!(this.active = active) && wasActive) {
            Player player = Bukkit.getPlayer(uuid);
            if(player != null)
                DisguiseHandler.INSTANCE.undisguisePlayer(player);
        }
    }
}
