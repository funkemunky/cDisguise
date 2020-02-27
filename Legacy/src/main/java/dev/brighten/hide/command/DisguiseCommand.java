package dev.brighten.hide.command;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import dev.brighten.hide.handler.DisguiseHandler;

@Init(commands = true)
public class DisguiseCommand {

    @Command(name = "disguise", description = "Disguise yourself as another player",
            display = "disguise", playerOnly = true, permission = {"cdisguise.command"})
    public void onCommand(CommandAdapter cmd) {
        if(cmd.getArgs().length > 0) {
            String nick = cmd.getArgs()[0];
            DisguiseHandler.INSTANCE.setPlayerNickname(cmd.getPlayer(), nick);
            DisguiseHandler.INSTANCE.setPlayerSkin(cmd.getPlayer(), nick);
            cmd.getSender().sendMessage(Color.Gray + "Disguised as player " + Color.Yellow + nick + Color.Gray + ".");
        } else cmd.getSender().sendMessage(Color.Red + "Invalid arguments.");
    }
}
