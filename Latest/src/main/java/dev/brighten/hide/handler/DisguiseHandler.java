package dev.brighten.hide.handler;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.events.AtlasListener;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.RunUtils;
import com.mojang.authlib.GameProfile;
import dev.brighten.hide.Disguise;
import dev.brighten.hide.disguise.DisguiseObject;
import dev.brighten.hide.game.GameProfileBuilder;
import dev.brighten.hide.reflection.DisguiseUtil;
import dev.brighten.hide.user.User;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.shanerx.mojang.PlayerProfile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Init
public class DisguiseHandler implements AtlasListener {
    public static DisguiseHandler INSTANCE;

    public Map<String, User> disguisedNames = new HashMap<>();
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
                all.hidePlayer(player);
                all.showPlayer(player);
            }
        }, Disguise.INSTANCE);
    }

    public void disguisePlayer(Player player, String name, String group) {
        Disguise.INSTANCE.disguiseThread.execute(() -> {
            User user = User.getUser(player);
            val target = Bukkit.getOfflinePlayer(name);
            setPlayerSkin(player, target.getName());
            setPlayerNickname(player, target.getName());

            DisguiseObject object = new DisguiseObject(player.getUniqueId(), target.getName(), group);

            if(user.disguise == null || !user.disguise.isActive()) {
                user.setOriginalGroup(Disguise.INSTANCE.vaultHandler.permission.getPrimaryGroup(player));
                user.originalPrefix = Disguise.INSTANCE.vaultHandler.chat.getPlayerPrefix(player);
            }

            Disguise.INSTANCE.vaultHandler.chat.setPlayerPrefix(player,
                    Disguise.INSTANCE.vaultHandler.chat.getGroupPrefix(player.getWorld(), group));

            user.disguise = object;
            user.disguise.setActive(true);

            disguisedNames.put(name, user);
            try {
                Disguise.INSTANCE.syncHandler.upSync(user);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void undisguisePlayer(Player player) {
        Disguise.INSTANCE.disguiseThread.execute(() -> {
            User user = User.getUser(player);

            if(user.disguise != null && user.disguise.isActive()) {
                String name = user.getPlayer().getName();
                try {
                    name = GameProfileBuilder.fetch(user.uuid).getName();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                setPlayerSkin(player, name);
                setPlayerNickname(player, name);

                Disguise.INSTANCE.vaultHandler.chat.setPlayerPrefix(player, user.originalPrefix);

                user.disguise.setActive(false);
                disguisedNames.remove(user.disguise.playerName);
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
