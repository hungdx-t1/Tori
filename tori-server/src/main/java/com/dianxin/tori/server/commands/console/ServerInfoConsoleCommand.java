package com.dianxin.tori.server.commands.console;

import com.dianxin.core.api.console.commands.AbstractConsoleCommand;
import com.dianxin.tori.api.base.Constants;
import net.dv8tion.jda.api.JDAInfo;

public class ServerInfoConsoleCommand extends AbstractConsoleCommand {
    public ServerInfoConsoleCommand() {
        super("serverinfo");
    }

    @Override
    public void execute(String[] args) {
        // Calculate Memory Usage
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long maxMemory = runtime.maxMemory() / (1024 * 1024);

        // Build a beautiful ASCII panel
        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================\n");
        sb.append("          🦅 TORI SERVER INFO\n");
        sb.append("========================================\n");
        sb.append(" 🔹 Tori Version : v").append(Constants.TORI_SERVER_VERSION).append("\n");
        sb.append(" 🔹 JDA Version  : v").append(JDAInfo.VERSION).append("\n");
        sb.append(" 🔹 Java Version : ").append(System.getProperty("java.version"))
                .append(" (Requires ").append(Constants.JAVA_REQUIRED_VERSION).append("+)\n");
        sb.append(" 🔹 OS           : ").append(System.getProperty("os.name")).append("\n");
        sb.append(" 🔹 Memory Usage : ").append(usedMemory).append(" MB / ").append(maxMemory).append(" MB\n");
        sb.append("========================================");

        getLogger().info(sb.toString());
    }
}
