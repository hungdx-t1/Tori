package com.dianxin.tori.server.commands.console;

import com.dianxin.core.api.console.commands.AbstractConsoleCommand;
import com.dianxin.tori.server.Main;

public class RestartConsoleCommand extends AbstractConsoleCommand {
    public RestartConsoleCommand() {
        super("restart");
    }

    @Override
    public void execute(String[] args) {
        Main.getServer().shutdown();
        System.exit(42);  // Trả về mã 42 để báo hiệu muốn Restart
    }
}