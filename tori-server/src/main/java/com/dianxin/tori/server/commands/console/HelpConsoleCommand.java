package com.dianxin.tori.server.commands.console;

import com.dianxin.core.api.console.commands.AbstractConsoleCommand;

public class HelpConsoleCommand extends AbstractConsoleCommand {
    public HelpConsoleCommand() {
        super("help");
    }

    @Override
    public void execute(String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================\n");
        sb.append("         🦅 TORI SERVER COMMANDS\n");
        sb.append("========================================\n");
        sb.append(" 🔹 help       - Displays this help menu.\n");
        sb.append(" 🔹 serverinfo - Displays server and system information.\n");
        sb.append(" 🔹 bots       - Lists all currently running bots.\n");
        sb.append(" 🔹 ping       - Checks the console responsiveness.\n");
        sb.append(" 🔹 stop       - Safely shuts down all bots and stops the server.\n");
        sb.append("========================================");

        getLogger().info(sb.toString());
    }
}