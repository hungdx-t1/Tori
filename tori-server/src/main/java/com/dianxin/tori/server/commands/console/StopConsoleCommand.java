package com.dianxin.tori.server.commands.console;

import com.dianxin.tori.base.console.commands.AbstractConsoleCommand;
import com.dianxin.tori.server.Main;

public class StopConsoleCommand extends AbstractConsoleCommand {
    public StopConsoleCommand() {
        super("stop");
    }

    @Override
    public void execute(String[] args) {
        Main.getServer().shutdown();
        System.exit(0);
    }
}