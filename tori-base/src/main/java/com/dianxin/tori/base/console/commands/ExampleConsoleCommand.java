package com.dianxin.tori.base.console.commands;

/**
 * A concrete implementation example of a console command.
 * Triggers a graceful JVM shutdown sequence when the "stop" command is entered.
 */
@SuppressWarnings("unused")
public class ExampleConsoleCommand extends AbstractConsoleCommand {

    /**
     * Constructs the example shutdown command under the trigger keyword "stop".
     */
    public ExampleConsoleCommand() {
        super("stop");
    }

    /**
     * Executes the shutdown logic, providing a hook to safely clean up open resources.
     *
     * @param args arguments passed from the console (ignored for this command)
     */
    @Override
    public void execute(String[] args) {
        // Place database connection teardowns or executor shutdowns here if needed.
        System.exit(0);
    }
}