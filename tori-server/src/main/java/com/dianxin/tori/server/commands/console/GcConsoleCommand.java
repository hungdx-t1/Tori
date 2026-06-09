package com.dianxin.tori.server.commands.console;

import com.dianxin.tori.base.console.commands.AbstractConsoleCommand;

public class GcConsoleCommand extends AbstractConsoleCommand {

    public GcConsoleCommand() {
        super("gc");
    }

    @Override
    public void execute(String[] args) {
        getLogger().info("⏳ Memory cleanup is being requested (Garbage Collection)...");

        long beforeUsed = getUsedMemory();
        System.gc();
        long afterUsed = getUsedMemory();

        long freed = beforeUsed - afterUsed;

        getLogger().info(
                "🧹 Cleaned! \n- Before: {} MB\n- After: {} MB\n- Freed: {} MB",
                bytesToMB(beforeUsed),
                bytesToMB(afterUsed),
                bytesToMB(freed)
        );
    }

    // Utility function to calculate the amount of RAM currently in use (Used = Total - Free)
    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private long bytesToMB(long bytes) {
        return bytes / 1024 / 1024;
    }
}

