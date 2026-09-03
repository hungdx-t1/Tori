package com.dianxin.tori.api.commands.slash.pending;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jspecify.annotations.NonNull;

import java.util.List;

public interface Command {
    @NonNull String name();
    @NonNull String description();
    @NonNull default List<OptionData> options() { return List.of(); }

}
