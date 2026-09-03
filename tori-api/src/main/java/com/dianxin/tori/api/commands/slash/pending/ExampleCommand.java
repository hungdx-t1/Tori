package com.dianxin.tori.api.commands.slash.pending;

import org.jspecify.annotations.NonNull;

public class ExampleCommand extends Command4Bot {


    public static class ExampleCommandData implements ICommandData {
        @Override
        public @NonNull String name() {
            return "example";
        }

        @Override
        public @NonNull String description() {
            return "Example Command";
        }
    }
}
