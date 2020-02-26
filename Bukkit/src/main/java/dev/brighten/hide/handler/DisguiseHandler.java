package dev.brighten.hide.handler;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.events.AtlasListener;
import cc.funkemunky.api.events.Listen;
import cc.funkemunky.api.events.impl.PacketSendEvent;
import cc.funkemunky.api.reflections.Reflections;
import cc.funkemunky.api.reflections.types.WrappedClass;
import cc.funkemunky.api.reflections.types.WrappedField;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutEntityMetadata;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutSpawnEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.types.WrappedWatchableObject;
import cc.funkemunky.api.utils.Init;
import dev.brighten.hide.Disguise;
import dev.brighten.hide.user.User;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

@Init
public class DisguiseHandler implements AtlasListener {
    public static DisguiseHandler INSTANCE;

    private Map<String, UUID> disguisedNames = new HashMap<>();

    public DisguiseHandler() {
        INSTANCE = this;
    }

    @Listen
    public void onEvent(PacketSendEvent event) {
        if(event.getType().equalsIgnoreCase(Packet.Server.ENTITY_METADATA)) {
            WrappedOutEntityMetadata packet = new WrappedOutEntityMetadata(event.getPacket(), event.getPlayer());

            val wrappedObjects = packet.getWatchableObjects().stream()
                    .map(WrappedWatchableObject::new)
                    .collect(Collectors.toList());

            boolean didChange = false;
            for (WrappedWatchableObject obj : wrappedObjects) {
                if (obj.getWatchedObject() instanceof String) {
                    String name = (String) obj.getWatchedObject();

                    /*if (disguisedNames.containsKey(name) || name.equals("funkemunky")) {
                        UUID uuid = name.equals("funkemunky")
                                ? Bukkit.getPlayer("funkemunky").getUniqueId()
                                : disguisedNames.get(name);

                        User user = User.getUser(uuid);

                        if (user.disguised) {
                            obj.setWatchedObject(user.disguise.playerName);
                            obj.setPacket(obj.getFirstInt(),
                                    obj.getDataValueId(), obj.getWatchedObject());
                            didChange = true;
                            break;
                        }
                    }*/

                    if(Bukkit.getWorlds().stream().anyMatch(world -> Atlas.getInstance().getEntities()
                            .get(world.getUID()).stream()
                            .anyMatch(entity -> entity.getName().equals(name)))) {
                        obj.setWatchedObject("nibba" + name);
                        didChange = true;
                        obj.setPacket(obj.getFirstInt(),
                                obj.getDataValueId(), obj.getWatchedObject());
                        System.out.println("changed");
                    }
                }
            }

            if(didChange) {
                event.setCancelled(true);
                TinyProtocolHandler.sendPacket(event.getPlayer(), new WrappedOutEntityMetadata(packet.getEntityId(), wrappedObjects.stream()
                        .map(WrappedWatchableObject::getObject)
                        .collect(Collectors.toList())).getObject());
            }
        } else if(event.getType().equals(Packet.Server.NAMED_ENTITY_SPAWN)) {
            WrappedClass osel = Reflections.getNMSClass(Packet.Server.NAMED_ENTITY_SPAWN),
            dataWatcher = Reflections.getNMSClass("DataWatcher");

            val mapField = dataWatcher.getFieldByType(Map.class, 1);


            WrappedField field = osel.getFieldByType(List.class, 0),
                    dataWatcherField = osel.getFieldByType(dataWatcher.getParent(), 0);

            Object dwObject = dataWatcherField.get(event.getPacket());

            Map<Integer, Object> map = mapField.get(dwObject);

            Map<Integer, WrappedWatchableObject> wrappedObjects = new HashMap<>();
            for (Integer key : map.keySet()) {
                wrappedObjects.put(key, new WrappedWatchableObject(map.get(key)));
            }

            boolean didChange = false;

            val uuidField = osel.getFieldByType(UUID.class, 0);
            UUID uuid = uuidField.get(event.getPacket());

            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

            if(uuid != null && player.getName() != null && player.getName().equals("funkemunky")) {
                for (Integer key : wrappedObjects.keySet()) {
                    map.put(key, wrappedObjects.get(key).getObject());
                }
                mapField.set(dwObject, map);
                uuidField.set(event.getPacket(), Bukkit.getOfflinePlayer("JavaNative").getUniqueId());
                dataWatcherField.set(event.getPacket(), dwObject);
            }
        }
    }

    public void disguisePlayer(Player player) {

    }
}
