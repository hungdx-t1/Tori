package com.dianxin.tori.base.console.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Manages the registration, parsing, and execution routing of console commands.
 * It listens to standard input (System.in) on a separate background thread.
 */
@SuppressWarnings("unused")
public class ConsoleCommandManager {

    private final Map<String, AbstractConsoleCommand> commands = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(ConsoleCommandManager.class);

    /**
     * Registers a new console command.
     * Logging behavior defaults to standard reporting.
     *
     * @param command the console command instance to register
     */
    public void register(AbstractConsoleCommand command) {
        this.register(command, false);
    }

    /**
     * Registers a console command with a flag to determine explicit log output.
     * Maps the command using its lower-case command identifier as the unique key.
     *
     * @param cmd          the console command instance to register
     * @param willPrintLog flag to indicate whether specific debug logs should be printed
     */
    public void register(AbstractConsoleCommand cmd, boolean willPrintLog) {
        commands.put(cmd.getCommandLine().toLowerCase(), cmd);
        if(willPrintLog) {
            logger.info("✅ Registered console command: {}", cmd.getCommandLine());
        }
    }

    /**
     * Initializes and starts a background thread that continuously reads inputs
     * from standard input (System.in) and routes them to their registered commands.
     */
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
                        logger.error("An error occurred when trying to execute command '{}': {}", name, e.getMessage());
                    }
                } else {
                    logger.warn("Command '{}' not found.", name);
                }
            }
        }, "ConsoleCommandListener");

        t.setDaemon(false);
        t.start();
        logger.info("ConsoleCommandListener started successfully.");
    }
}