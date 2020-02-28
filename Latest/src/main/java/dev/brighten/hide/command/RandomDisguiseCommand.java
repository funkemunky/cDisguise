package dev.brighten.hide.command;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.ItemBuilder;
import dev.brighten.hide.Disguise;
import dev.brighten.hide.utils.menu.button.Button;
import dev.brighten.hide.utils.menu.preset.button.FillerButton;
import dev.brighten.hide.utils.menu.type.impl.ChestMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Init(commands = true)
public class RandomDisguiseCommand {

    @ConfigSetting(name = "randomNames")
    private static List<String> randomNames = Arrays.asList("funkemunky", "test", "CopyOnWriteArray", "LeftBoob", "dewfs");

    @Command(name = "randomdisguise", aliases = {"rdisguise"}, playerOnly = true,
            description = "Disguise yourself as a random.", permission = {"cdisguise.command.rdisguise"})
    public void onCommand(CommandAdapter cmd) {
        String name = randomNames.get(ThreadLocalRandom.current().nextInt(0, randomNames.size() - 1));
        if(cmd.getArgs().length == 0) {
            getRanksMenu(name).showMenu(cmd.getPlayer());
        } else if(cmd.getArgs().length == 1) {
            Bukkit.dispatchCommand(cmd.getPlayer(), "disguise " + name + " " + cmd.getArgs()[0]);
        }
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
