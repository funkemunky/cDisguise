package dev.brighten.hide.handler;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.events.AtlasListener;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.RunUtils;
import com.mojang.authlib.GameProfile;
import dev.brighten.hide.Disguise;
import dev.brighten.hide.disguise.DisguiseObject;
import dev.brighten.hide.reflection.DisguiseUtil;
import dev.brighten.hide.game.GameProfileBuilder;
import dev.brighten.hide.user.User;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.shanerx.mojang.PlayerProfile;

import java.io.IOException;
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

        RunUtils.task(() -> {
            for(Player all : Bukkit.getOnlinePlayers()) {
                try {
                    all.hidePlayer(Disguise.INSTANCE, player);
                    all.showPlayer(Disguise.INSTANCE, player);
                } catch (NoSuchMethodError e) {
                    all.hidePlayer(player);
                    all.showPlayer(player);
                }
            }
        }, Disguise.INSTANCE);
    }

    public void disguisePlayer(Player player, String name, String group) {
        Disguise.INSTANCE.disguiseThread.execute(() -> {
            val target = Bukkit.getOfflinePlayer(name);
            setPlayerSkin(player, target.getName());
            setPlayerNickname(player, target.getName());

            DisguiseObject object = new DisguiseObject(target.getUniqueId(), target.getName(), group);

            User user = User.getUser(player.getUniqueId());
            user.setOriginalGroup(Disguise.INSTANCE.vaultHandler.permission.getPrimaryGroup(player));

            Disguise.INSTANCE.vaultHandler.chat.setPlayerPrefix(player,
                    Disguise.INSTANCE.vaultHandler.chat.getGroupPrefix(player.getWorld(), group));

            object.active = true;
            try {
                Disguise.INSTANCE.syncHandler.upSync(user);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void undisguisePlayer(Player player) {
        Disguise.INSTANCE.disguiseThread.execute(() -> {
            User user = User.getUser(player.getUniqueId());

            if(user.disguise != null && user.disguise.active) {
                OfflinePlayer opl = Bukkit.getOfflinePlayer(player.getUniqueId());

                setPlayerNickname(player, opl.getName());
                setPlayerSkin(player, opl.getName());

                Disguise.INSTANCE.vaultHandler.chat.setPlayerPrefix(player, user.originalPrefix);

                user.disguise.active = false;
                try {
                    Disguise.INSTANCE.syncHandler.upSync(user);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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
