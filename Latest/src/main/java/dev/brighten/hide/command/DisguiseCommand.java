package dev.brighten.hide.command;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.ItemBuilder;
import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.hide.Disguise;
import dev.brighten.hide.handler.DisguiseHandler;
import dev.brighten.hide.utils.menu.button.Button;
import dev.brighten.hide.utils.menu.preset.button.FillerButton;
import dev.brighten.hide.utils.menu.type.anvil.AnvilGUI;
import dev.brighten.hide.utils.menu.type.impl.ChestMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;

@Init(commands = true)
public class DisguiseCommand {

    @Command(name = "disguise", description = "Disguise yourself as another player",
            display = "disguise", playerOnly = true, permission = {"cdisguise.command.disguise"})
    public void onCommand(CommandAdapter cmd) {
        if(cmd.getArgs().length > 1) {
            String nick = cmd.getArgs()[0];
            if(Bukkit.getPlayer(nick) != null) {
                cmd.getSender().sendMessage(Color.Red + "You cannot disguise yourself as a player that is online.");
                return;
            }
            DisguiseHandler.INSTANCE.disguisePlayer(cmd.getPlayer(), nick, cmd.getArgs()[1]);
            cmd.getSender().sendMessage(Color.Gray + "Disguised as player " + Color.Yellow + nick + Color.Gray + ".");
        } else {
            getValueMenu(cmd.getPlayer());
        }
    }

    @Command(name = "undisguise", description = "Undisguise yourself", display = "undisguise",
            playerOnly = true, permission = "cdisguise.command.undisguise")
    public void onUndisguise(CommandAdapter cmd) {
        cmd.getSender().sendMessage(Color.Gray + "Removing any disguise you may have had...");
        DisguiseHandler.INSTANCE.undisguisePlayer(cmd.getPlayer());
    }


    private void getValueMenu(Player player) {
        AnvilGUI gui = new AnvilGUI(player, (event) -> {
            if(event.getSlot().getSlot() == 2) {
                RunUtils.taskLater(() -> {
                    ChestMenu menu = getRanksMenu(event.getName());

                    menu.showMenu(player);
                }, Disguise.INSTANCE, 2);
                System.out.println("did it");
                event.setWillClose(true);
                event.setWillDestroy(true);
            }
        });

        gui.setSlot(AnvilGUI.AnvilSlot.INPUT_LEFT, new ItemBuilder(Material.PAPER).amount(1).name("Input").build());

        gui.open();
    }

    private ChestMenu getRanksMenu(String value) {
        ChestMenu menu = new ChestMenu("Choose a rank", 4);
        Disguise.INSTANCE.vaultHandler.getRanks().stream()
                .sorted(Comparator.comparing(rank -> rank))
                .forEach(rank -> {
                    Button button = getRankButton(value, rank);

                    menu.addItem(button);
                });
        menu.fill(new FillerButton());

        return menu;
    }

    private Button getRankButton(String value, String rank) {
        ItemStack stack = new ItemBuilder(Material.BOOK).amount(1)
                .name(Color.Gold + rank).build();

        return new Button(false, stack, (player, info) -> {
            info.getMenu().close(player);
            Bukkit.dispatchCommand(player, "disguise " + value + " " + rank);
        });
    }
}
