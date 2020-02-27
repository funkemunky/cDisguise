package dev.brighten.hide.handler;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.events.AtlasListener;
import cc.funkemunky.api.utils.Init;
import dev.brighten.hide.Disguise;
import dev.brighten.hide.reflection.DisguiseUtil;
import dev.brighten.hide.game.GameProfileBuilder;
import lombok.SneakyThrows;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.shanerx.mojang.PlayerProfile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Init
public class DisguiseHandler implements AtlasListener {
    public static DisguiseHandler INSTANCE;

    private Map<String, UUID> disguisedNames = new HashMap<>();
    private static Map<String, String> cachedUUIDs = new HashMap<>();
    private static Map<String, PlayerProfile> cachedProfile = new HashMap<>();

    public DisguiseHandler() {
        INSTANCE = this;
    }

    public void setPlayerNickname(Player player, String name) {
        DisguiseUtil.setNameField(player, name);
        DisguiseUtil.removePlayerFromTabList(player);
        DisguiseUtil.addPlayerToTabList(player);
    }

    @SneakyThrows
    public void setPlayerSkin(Player player, String skin) {
        UUID uuid = Bukkit.getOfflinePlayer(skin).getUniqueId();
        GameProfile profile = GameProfileBuilder.fetch(uuid);
        DisguiseUtil.removeProperty(player, "textures");
        DisguiseUtil.insertProperty(player, "textures", profile.getProperties().get("textures"));
        DisguiseUtil.removePlayerFromTabList(player);
        DisguiseUtil.addPlayerToTabList(player);
        DisguiseUtil.respawn(player);

        for(Player all : Bukkit.getOnlinePlayers()) {
            try {
                all.hidePlayer(Disguise.INSTANCE, player);
                all.showPlayer(Disguise.INSTANCE, player);
            } catch (NoSuchMethodError e) {
                all.hidePlayer(player);
                all.showPlayer(player);
            }
        }
    }

    public String getUUID(String name) {
        return cachedUUIDs.computeIfAbsent(name, key -> {
            String uuid = Atlas.getInstance().getMojang().getUUIDOfUsername(name);
            cachedUUIDs.put(key, uuid);

            return uuid;
        });
    }

    public PlayerProfile getProfile(String uuid) {
        return cachedProfile.computeIfAbsent(uuid, key -> {
           PlayerProfile profile = Atlas.getInstance().getMojang().getPlayerProfile(uuid);
           cachedProfile.put(key, profile);

           return profile;
        });
    }
}
