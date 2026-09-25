package com.dianxin.tori.server.commands.console;

import com.dianxin.tori.base.annotations.ReleasedSince;
import com.dianxin.tori.base.console.commands.AbstractConsoleCommand;
import com.dianxin.tori.server.Main;

@ReleasedSince("26.4.224")
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