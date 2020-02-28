package dev.brighten.hide.utils.menu.type.impl;

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
import cc.funkemunky.api.utils.XMaterial;
import dev.brighten.hide.utils.menu.Menu;
import dev.brighten.hide.utils.menu.button.Button;
import dev.brighten.hide.utils.menu.type.BukkitInventoryHolder;
import dev.brighten.hide.utils.menu.util.ArrayIterator;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

public class AnvilMenu implements Menu {

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
    @Setter
    private Menu parent;
    @Getter
    @Setter
    private String title;
    @Getter
    private BukkitInventoryHolder holder;
    @Setter
    private CloseHandler closeHandler;
    @Getter
    private MenuDimension menuDimension;
    private Button[] contents;
    public final BiConsumer<Integer, String> clickHandler;

    public AnvilMenu(String title, BiConsumer<Integer, String> clickHandler) {
        this.title = title;
        menuDimension = new MenuDimension(1, 3);
        contents = new Button[menuDimension.getSize()];
        this.clickHandler = clickHandler;
    }

    @Override
    public Optional<Menu> getParent() {
        return Optional.ofNullable(parent);
    }

    @Override
    public void addItem(Button button) {
        setItem(getFirstEmptySlot(), button);
    }

    @Override
    public void setItem(int index, Button button) {
        checkBounds(index);
        contents[index] = button;
    }

    @Override
    public void fill(Button button) {
        fillRange(0, menuDimension.getSize(), button);
    }

    @Override
    public void fillRange(int startingIndex, int endingIndex, Button button) {
        IntStream.range(startingIndex, endingIndex)
                .filter(i -> contents[i] == null || contents[i].getStack().getType()
                        .equals(XMaterial.AIR.parseMaterial()))
                .forEach(i -> setItem(i, button));
    }

    @Override
    public int getFirstEmptySlot() {
        for (int i = 0; i < contents.length; i++) {
            Button button = contents[i];
            if (button == null) {
                return i;
            }
        }
        return -1; // Will throw when #checkBounds is called.
    }

    @Override
    public void checkBounds(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index > (menuDimension.getSize())) {
            throw new IndexOutOfBoundsException(String.format("setItem(); %s is out of bounds!", index));
        }
    }

    @Override
    public Optional<Button> getButtonByIndex(int index) {
        return Optional.empty();
    }

    @Override
    public void buildInventory(boolean initial) {

    }

    @Override
    public void showMenu(Player player) {
        if(holder == null) buildInventory(true);
        else buildInventory(false);
        Object p = CraftReflection.getEntityPlayer(player);


        Object container = canvilConst.newInstance(CraftReflection.getVanillaInventory(player),
                CraftReflection.getVanillaWorld(player.getWorld()),
                new BaseBlockPosition(0,0,0).getAsBlockPosition(), p);
        checkReachable.set(container, false);

        //Set the items to the items from the inventory given
        InventoryView bukkitView = getBukkitView.invoke(container);
        Inventory inv = bukkitView.getTopInventory();

        for (int i = 0; i < contents.length; i++) {
            Button button = contents[i];
            if(button == null) continue;
            System.out.println("set " + i);
            inv.setItem(i, button.getStack());
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
            addSlot.invoke(container, p);
            player.setLevel(player.getLevel() + 1);
        }
    }

    @Override
    public void close(Player player) {
        player.closeInventory();
        handleClose(player);
    }

    @Override
    public void handleClose(Player player) {
        for (Button content : contents) {
            if(content == null) continue;

            content.setStack(new ItemStack(Material.AIR, 1));
        }
        if(closeHandler != null)
            closeHandler.accept(player, this);
    }


    @Override
    public Iterator<Button> iterator() {
        return new ArrayIterator<>(contents);
    }
}
