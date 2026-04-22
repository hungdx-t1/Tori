package com.dianxin.tori.server.commands.console;

import com.dianxin.core.api.console.commands.AbstractConsoleCommand;

public class PingConsoleCommand extends AbstractConsoleCommand {
    public PingConsoleCommand() {
        super("ping");
    }

    @Override
    public void execute(String[] args) {
        getLogger().info("Pong!");
    }
}
