package dev.brighten.hide.command;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import dev.brighten.hide.Disguise;
import dev.brighten.hide.handler.DisguiseHandler;

@Init(commands = true)
public class DisguiseCommand {

    @Command(name = "disguise", description = "Disguise yourself as another player",
            display = "disguise", playerOnly = true, permission = {"cdisguise.command"})
    public void onCommand(CommandAdapter cmd) {
        if(cmd.getArgs().length > 1) {
            String nick = cmd.getArgs()[0];
            DisguiseHandler.INSTANCE.disguisePlayer(cmd.getPlayer(), nick, cmd.getArgs()[1]);
            cmd.getSender().sendMessage(Color.Gray + "Disguised as player " + Color.Yellow + nick + Color.Gray + ".");
        } else cmd.getSender().sendMessage(Color.Red + "Invalid arguments.");
    }

    @Command(name = "undisguise", description = "Undisguise yourself", display = "undisguise",
            playerOnly = true, permission = "cdisguise.command")
    public void onUndisguise(CommandAdapter cmd) {
        cmd.getSender().sendMessage(Color.Gray + "Removing any disguise you may have had...");
        DisguiseHandler.INSTANCE.undisguisePlayer(cmd.getPlayer());
    }
}
