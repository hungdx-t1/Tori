package com.dianxin.tori.base.console.commands;

@SuppressWarnings("unused")
public class ExampleConsoleCommand extends AbstractConsoleCommand {

    public ExampleConsoleCommand() {
        super("stop");
    }

    @Override
    public void execute(String[] args) {
        // các phương thức đóng DB hoặc close executor
        System.exit(0);
    }
}