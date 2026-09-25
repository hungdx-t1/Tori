package com.dianxin.tori.server.commands.console;

import com.dianxin.tori.base.annotations.ReleasedSince;
import com.dianxin.tori.base.console.commands.AbstractConsoleCommand;
import com.dianxin.tori.server.updater.UpdateChecker;

@ReleasedSince("26.9.1")
public class UpdateConsoleCommand extends AbstractConsoleCommand {
    public UpdateConsoleCommand() {
        super("update");
    }

    @Override
    public void execute(String[] args) {
        getLogger().info("Checking for updates...");
        UpdateChecker.checkForUpdateAsync().queue(
                success -> {},
                error -> getLogger().error("Error while checking for updates.", error)
        );
    }
}
