package dev.brighten.hide.reflection;

import cc.funkemunky.api.reflections.Reflections;
import cc.funkemunky.api.reflections.impl.CraftReflection;
import cc.funkemunky.api.reflections.types.WrappedClass;
import cc.funkemunky.api.reflections.types.WrappedField;
import cc.funkemunky.api.reflections.types.WrappedMethod;
import cc.funkemunky.api.tinyprotocol.api.NMSObject;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutChatPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutPlayerInfo;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutRespawnPacket;
import cc.funkemunky.api.tinyprotocol.packet.types.WrappedChatMessageType;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumDifficulty;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumGameMode;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumPlayerInfoAction;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

public class DisguiseUtil {

    /** Wrapped Classes **/
    private static WrappedClass gameProfile = Reflections.getClass("com.mojang.authlib.GameProfile");
    private static WrappedClass propertyMap = Reflections.getClass("com.google.common.collect.ForwardingMultimap");

    /** Wrapped Methods **/
    private static WrappedMethod getProfile = CraftReflection.craftPlayer.getMethod("getProfile");
    private static WrappedMethod getProperties = gameProfile.getMethod("getProperties");
    private static WrappedMethod removeAll = propertyMap.getMethod("removeAll", Object.class);
    private static WrappedMethod putAll = propertyMap.getMethod("putAll", Object.class, Iterable.class);

    /** Wrapped Field **/
    private static WrappedField nameField = gameProfile.getFieldByName("name");

    public static void removeProperty(Player player, String property) {
        Object profile = getProfile.invoke(player);
        Object properties = getProperties.invoke(profile);

        removeAll.invoke(properties,
                property);
    }

    public static void insertProperty(Player player, String property, Collection<Property> properties) {
        Object profile = getProfile.invoke(player);
        Object propertyMap = getProperties.invoke(profile);

        /*for (PlayerProfile.Property property1 : properties) {
            System.out.println(property1.getName() + ";" + property1.getValue() + ";" + property1.getSignature());
        }*/

        /*putAll.invoke(propertyMap, property, properties.stream()
                .map(MojangUtils::toMojangProperty).collect(Collectors.toList()));*/
        putAll.invoke(propertyMap, property, properties);
    }

    public static void setNameField(Player player, String name) {
        Object profile = getProfile.invoke(player);
        nameField.set(profile, name);
    }

    public static void addPlayerToTabList(Player player) {
        WrappedOutPlayerInfo playerInfo = new WrappedOutPlayerInfo(WrappedEnumPlayerInfoAction.ADD_PLAYER, player);

        sendPacket(playerInfo);
    }

    public static void removePlayerFromTabList(Player player) {
        WrappedOutPlayerInfo playerInfo = new WrappedOutPlayerInfo(WrappedEnumPlayerInfoAction.REMOVE_PLAYER, player);

        sendPacket(playerInfo);
    }

    public static void respawn(Player player) {
        WrappedOutRespawnPacket packet = new WrappedOutRespawnPacket(player.getWorld().getEnvironment().getId(),
                WrappedEnumGameMode.getByName(player.getGameMode().name()),
                WrappedEnumDifficulty.getByName(player.getWorld().getDifficulty().name()),
                player.getWorld().getWorldType());

        sendPacket(player, packet);
    }

    public static void sendActionBar(Player player, String message) {
        WrappedOutChatPacket packet = new WrappedOutChatPacket("{\"text\": \"" + message + "\"}",
                WrappedChatMessageType.GAME_INFO);

        sendPacket(player, packet);
    }

    private static void sendPacket(NMSObject object) {
        for (Player opl : Bukkit.getOnlinePlayers()) {
            TinyProtocolHandler.sendPacket(opl, object.getObject());
        }
    }

    private static void sendPacket(Player player, NMSObject object) {
        TinyProtocolHandler.sendPacket(player, object.getObject());
    }
}
