package com.dianxin.tori.api.commands.slash.pending;

import org.jspecify.annotations.NonNull;

public interface ICommandData {
    @NonNull String name();

    @NonNull String description();
}
