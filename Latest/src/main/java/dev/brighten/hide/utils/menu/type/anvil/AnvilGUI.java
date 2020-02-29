package dev.brighten.hide.utils.menu.type.anvil;

import cc.funkemunky.api.reflections.Reflections;
import cc.funkemunky.api.reflections.impl.CraftReflection;
import cc.funkemunky.api.reflections.impl.MinecraftReflection;
import cc.funkemunky.api.reflections.types.WrappedClass;
import cc.funkemunky.api.reflections.types.WrappedConstructor;
import cc.funkemunky.api.reflections.types.WrappedField;
import cc.funkemunky.api.reflections.types.WrappedMethod;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutOpenWindow;
import cc.funkemunky.api.tinyprotocol.packet.types.BaseBlockPosition;
import cc.funkemunky.api.tinyprotocol.packet.types.WrappedChatMessage;
import dev.brighten.hide.Disguise;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

/**
* Created by chasechocolate.
*/
public class AnvilGUI {
    private Player player;
    private AnvilClickEventHandler handler;
    private HashMap<AnvilSlot, ItemStack> items = new HashMap<AnvilSlot, ItemStack>();
    private Inventory inv;
    private Listener listener;

    private static WrappedClass chatMessage = Reflections.getNMSClass("ChatMessage"),
            containerAnvil = Reflections.getNMSClass("ContainerAnvil"),
            playerInventory = Reflections.getNMSClass("PlayerInventory"),
            container = Reflections.getNMSClass("Container"),
            iCrafting = Reflections.getNMSClass("ICrafting");
    private static WrappedConstructor canvilConst = containerAnvil
            .getConstructor(MinecraftReflection.playerInventory.getParent(), MinecraftReflection.world.getParent(),
                    MinecraftReflection.blockPos.getParent(), MinecraftReflection.entityHuman.getParent());
    private static WrappedField pinventory = MinecraftReflection.entityPlayer.getFieldByName("inventory"),
            checkReachable = container.getFieldByName("checkReachable"),
            activeCounter = MinecraftReflection.entityHuman.getFieldByName("activeContainer"),
            windowId = container.getFieldByName("windowId");
    private static WrappedMethod getBukkitView = container.getMethod("getBukkitView"),
            nextCont = MinecraftReflection.entityPlayer.getMethod("nextContainerCounter"),
            addSlot = container.getMethod("addSlotListener", iCrafting.getParent());

    public AnvilGUI(final Player player, final AnvilClickEventHandler handler) {
        this.player = player;
        this.handler = handler;

        if(!Disguise.INSTANCE.isEnabled()) return;

        this.listener = new Listener() {
            @EventHandler
            public void onInventoryClick(InventoryClickEvent event) {
                if (event.getWhoClicked() instanceof Player) {

                    if (event.getInventory().equals(inv)) {
                        event.setCancelled(true);

                        ItemStack item = event.getCurrentItem();
                        int slot = event.getRawSlot();
                        String name = "";

                        if (item != null) {
                            if (item.hasItemMeta()) {
                                ItemMeta meta = item.getItemMeta();

                                if (meta.hasDisplayName()) {
                                    name = meta.getDisplayName();
                                }
                            }
                        }

                        AnvilClickEvent clickEvent = new AnvilClickEvent(AnvilSlot.bySlot(slot), name);

                        handler.onAnvilClick(clickEvent);

                        if (clickEvent.getWillClose()) {
                            event.getWhoClicked().closeInventory();
                        }

                        if (clickEvent.getWillDestroy()) {
                            destroy();
                        }
                    }
                }
            }

            @EventHandler
            public void onInventoryClose(InventoryCloseEvent event) {
                if (event.getPlayer() instanceof Player) {
                    Inventory inv = event.getInventory();
                    player.setLevel(player.getLevel() - 1);
                    if (inv.equals(AnvilGUI.this.inv)) {
                        inv.clear();
                        destroy();
                    }
                }
            }

            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent event) {
                if (event.getPlayer().equals(getPlayer())) {
                    player.setLevel(player.getLevel() - 1);
                    destroy();
                }
            }
        };

        Bukkit.getPluginManager().registerEvents(listener, Disguise.INSTANCE); //Replace with instance of main class
    }

    public Player getPlayer() {
        return player;
    }

    public void setSlot(AnvilSlot slot, ItemStack item) {
        items.put(slot, item);
    }

    public void open() {
        /*player.setLevel(player.getLevel() + 1);

        try {
            Object p = NMSManager.get().getHandle(player);

            Object container = ContainerAnvil.getConstructor(NMSManager.get().getNMSClass("PlayerInventory"), NMSManager.get().getNMSClass("World"), BlockPosition, EntityHuman).newInstance(NMSManager.get().getPlayerField(player, "inventory"), NMSManager.get().getPlayerField(player, "world"), BlockPosition.getConstructor(int.class, int.class, int.class).newInstance(0, 0, 0), p);
            NMSManager.get().getField(NMSManager.get().getNMSClass("Container"), "checkReachable").set(container, false);

            //Set the items to the items from the inventory given
            Object bukkitView = NMSManager.get().invokeMethod("getBukkitView", container);
            inv = (Inventory) NMSManager.get().invokeMethod("getTopInventory", bukkitView);

            for (AnvilSlot slot : items.keySet()) {
                inv.setItem(slot.getSlot(), items.get(slot));
            }

            //Counter stuff that the game uses to keep track of inventories
            int c = (int) NMSManager.get().invokeMethod("nextContainerCounter", p);

            //Send the packet
            Constructor<?> chatMessageConstructor = ChatMessage.getConstructor(String.class, Object[].class);
            Object playerConnection = NMSManager.get().getPlayerField(player, "playerConnection");
            Object packet = PacketPlayOutOpenWindow.getConstructor(int.class, String.class, NMSManager.get().getNMSClass("IChatBaseComponent"), int.class).newInstance(c, "minecraft:anvil", chatMessageConstructor.newInstance("Repairing", new Object[]{}), 0);

            Method sendPacket = NMSManager.get().getMethod("sendPacket", playerConnection.getClass(), PacketPlayOutOpenWindow);
            sendPacket.invoke(playerConnection, packet);

            //Set their active container to the container
            Field activeContainerField = NMSManager.get().getField(EntityHuman, "activeContainer");
            if (activeContainerField != null) {
                activeContainerField.set(p, container);

                //Set their active container window id to that counter stuff
                NMSManager.get().getField(NMSManager.get().getNMSClass("Container"), "windowId").set(activeContainerField.get(p), c);

                //Add the slot listener
                NMSManager.get().getMethod("addSlotListener", activeContainerField.get(p).getClass(), p.getClass()).invoke(activeContainerField.get(p), p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        player.setLevel(player.getLevel() + 1);

        Object p = CraftReflection.getEntityPlayer(player);

        Object container = canvilConst.newInstance(CraftReflection.getVanillaInventory(player),
                CraftReflection.getVanillaWorld(player.getWorld()),
                new BaseBlockPosition(0,0,0).getAsBlockPosition(), p);
        checkReachable.set(container, false);

        //Set the items to the items from the inventory given
        InventoryView bukkitView = getBukkitView.invoke(container);
        inv = bukkitView.getTopInventory();

        for (AnvilSlot slot : items.keySet()) {
            inv.setItem(slot.getSlot(), items.get(slot));
        }

        //Counter stuff that the game uses to keep track of inventories
        int c = nextCont.invoke(p);

        //Send the packet
       /* Constructor<?> chatMessageConstructor = ChatMessage.getConstructor(String.class, Object[].class);
        Object playerConnection = NMSManager.get().getPlayerField(player, "playerConnection");
        Object packet = PacketPlayOutOpenWindow.getConstructor(int.class, String.class, NMSManager.get().getNMSClass("IChatBaseComponent"), int.class).newInstance(c, "minecraft:anvil", chatMessageConstructor.newInstance("Repairing", new Object[]{}), 0);

        Method sendPacket = NMSManager.get().getMethod("sendPacket", playerConnection.getClass(), PacketPlayOutOpenWindow);
        sendPacket.invoke(playerConnection, packet);*/

        WrappedOutOpenWindow packet = new WrappedOutOpenWindow(c, "minecraft:anvil",
                new WrappedChatMessage("Repairing"), 0);

        TinyProtocolHandler.sendPacket(player, packet.getObject());

        //Set their active container to the container
        if (activeCounter != null) {
            activeCounter.set(p, container);

            //Set their active container window id to that counter stuff
            windowId.set(activeCounter.get(p), c);

            //Add the slot listener
            addSlot.invoke(activeCounter.get(p), p);
            player.setLevel(player.getLevel() + 1);
        }
    }

    public void destroy() {
        player = null;
        handler = null;
        items = null;

        HandlerList.unregisterAll(listener);

        listener = null;
    }

    public enum AnvilSlot {
        INPUT_LEFT(0),
        INPUT_RIGHT(1),
        OUTPUT(2);

        private int slot;

        private AnvilSlot(int slot) {
            this.slot = slot;
        }

        public static AnvilSlot bySlot(int slot) {
            for (AnvilSlot anvilSlot : values()) {
                if (anvilSlot.getSlot() == slot) {
                    return anvilSlot;
                }
            }

            return null;
        }

        public int getSlot() {
            return slot;
        }
    }

    @FunctionalInterface
    public interface AnvilClickEventHandler {
        void onAnvilClick(AnvilClickEvent event);
    }

    public class AnvilClickEvent {
        private AnvilSlot slot;

        private String name;

        private boolean close = true;
        private boolean destroy = true;

        public AnvilClickEvent(AnvilSlot slot, String name) {
            this.slot = slot;
            this.name = name;
        }

        public AnvilSlot getSlot() {
            return slot;
        }

        public String getName() {
            return name;
        }

        public boolean getWillClose() {
            return close;
        }

        public void setWillClose(boolean close) {
            this.close = close;
        }

        public boolean getWillDestroy() {
            return destroy;
        }

        public void setWillDestroy(boolean destroy) {
            this.destroy = destroy;
        }
    }
}