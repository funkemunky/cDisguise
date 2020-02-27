package dev.brighten.hide.disguise;

import dev.brighten.db.utils.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

@RequiredArgsConstructor
@AllArgsConstructor
public class DisguiseObject {
    public final UUID uuid;
    public final String playerName;
    public final String groupDisguise;
    public boolean active;

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
}
