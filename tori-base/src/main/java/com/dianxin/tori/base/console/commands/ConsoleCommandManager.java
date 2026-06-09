package com.dianxin.tori.base.console.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@SuppressWarnings("unused")
public class ConsoleCommandManager {

    private final Map<String, AbstractConsoleCommand> commands = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(ConsoleCommandManager.class);

    /** Đăng ký command */
    public void register(AbstractConsoleCommand cmd) {
        commands.put(cmd.getCommandLine().toLowerCase(), cmd);
        logger.info("✅ Registered console command: {}", cmd.getCommandLine());
    }

    /** Bắt đầu đọc console */
    public void startListening() {
        Scanner sc = new Scanner(System.in);

        Thread t = new Thread(() -> {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] split = line.split(" ");
                String name = split[0].toLowerCase();
                String[] args = Arrays.copyOfRange(split, 1, split.length);

                AbstractConsoleCommand cmd = commands.get(name);

                if (cmd != null) {
                    try {
                        cmd.execute(args);
                    } catch (Exception e) {
                        logger.error("An error occured when trying to execute command '{}': {}", name, e.getMessage());
                    }
                } else {
                    logger.warn("Command '{}' not found.", name);
                }
            }
        }, "ConsoleCommandListener");

        t.setDaemon(false);
        t.start();
        logger.info("ConsoleCommandListener start.");
    }
}