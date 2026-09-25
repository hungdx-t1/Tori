package com.dianxin.tori.server.commands.console;

import com.dianxin.tori.base.annotations.ReleasedSince;
import com.dianxin.tori.base.console.commands.AbstractConsoleCommand;

@ReleasedSince("26.4.231")
public class PingConsoleCommand extends AbstractConsoleCommand {
    public PingConsoleCommand() {
        super("ping");
    }

    @Override
    public void execute(String[] args) {
        getLogger().info("Pong!");
    }
}
